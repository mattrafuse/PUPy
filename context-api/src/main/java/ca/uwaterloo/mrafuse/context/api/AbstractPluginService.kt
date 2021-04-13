/**
 * Copyright 2016 - 2017
 *
 * Daniel Hintze <daniel.hintze></daniel.hintze>@fhdw.de>
 * Sebastian Scholz <sebastian.scholz></sebastian.scholz>@fhdw.de>
 * Rainhard D. Findling <rainhard.findling></rainhard.findling>@fh-hagenberg.at>
 * Muhammad Muaaz <muhammad.muaaz></muhammad.muaaz>@usmile.at>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.uwaterloo.mrafuse.context.api

import android.app.Service
import android.content.*
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity

/**
 * Manages the connection to the framework through service binding and messages.
 */
abstract class AbstractPluginService : Service() {
    private val logTag = javaClass.simpleName
    private val callbackMessenger = Messenger(MessageHandler())
    private var messenger: Messenger? = null
    abstract val pluginType: PLUGIN_TYPE

    /**
     * Implement what happens if the AuthenticationFramework asks for a data update.
     * Data can be send with the publishConfidenceUpdate resp. publishRiskUpdate method.
     */
    protected abstract fun onDataUpdateRequest()
    protected abstract fun onNewPublishedData(msg: Message)

    protected fun publishDataUpdate(dataBundle: Bundle?) {
        if (messenger == null) {
            Log.w(logTag, "Can't send " + pluginType.name + ": No connection to framework")
            return
        }

        val type = when (pluginType) {
            PLUGIN_TYPE.privacy -> PUPyConstants.MSG_PRIVACY
            PLUGIN_TYPE.unfamiliarity -> PUPyConstants.MSG_UNFAMILIARITY
            PLUGIN_TYPE.proximity -> PUPyConstants.MSG_PROXIMITY
            else -> return
        }

        val msg = Message.obtain(null, type)
        msg.data = dataBundle
        try {
            messenger!!.send(msg)
        } catch (e: RemoteException) {
            Log.e(logTag, "Can't send " + pluginType.name + "Data", e)
        }
    }

    internal inner class MessageHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PUPyConstants.MSG_POLL_DATA -> {
                    Log.d(logTag, "Received poll request for data")
                    onDataUpdateRequest()
                }
                PUPyConstants.MSG_PUBLISH_DATA -> {
                    Log.d(logTag, "Received new published data")
                    onNewPublishedData(msg)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            messenger = Messenger(service)
            try {
                val bundle = Bundle()
                bundle.putParcelable(PUPyConstants.KEY_COMPONENT_NAME, serviceComponentName)
                val msg = Message.obtain(null, PUPyConstants.MSG_ADD_PLUGIN)
                msg.data = bundle
                msg.replyTo = callbackMessenger
                messenger!!.send(msg)
                Log.d(logTag, this@AbstractPluginService.javaClass.simpleName + " connected")
            } catch (e: RemoteException) {
                Log.e(logTag, "Can't connect to Framework", e)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            messenger = null
            stopSelf()
            Log.d(logTag, this@AbstractPluginService.javaClass.simpleName + " disconnected")
        }
    }

    private fun bindToFramework() {
        val intent = Intent()
        val frameworkComponent = ComponentName(PUPyConstants.PUPY_PACKAGE,
                PUPyConstants.PUPY_PACKAGE + PUPyConstants.PUPY_SERVICE)

        Log.i(LOG_TAG, "bindToFramework: ${frameworkComponent.toString()}")

        //Check if there is a valid Framework installed
        if (!PermissionUtil.checkReadPluginDataPermission(logTag, this, frameworkComponent.packageName)) {
            Toast.makeText(this, "Can't bind to Framework: Service has not the required " + Permissions.READ_PLUGIN_DATA
                    + " permission", Toast.LENGTH_LONG).show()
        }
        intent.component = frameworkComponent
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindFromFramework() {
        unbindService(serviceConnection)
        //TODO it should also send a remove msg to the framework to update the plugin list or use packageChanged in pluginReceiver...
    }

    protected val serviceComponentName: ComponentName
        get() = ComponentName(packageName, javaClass.name)

    private fun setupLocalDataReceiver() {
        val filter = IntentFilter()
        filter.addAction(PUPyConstants.ACTION_LOCAL_SEND_PRIVACY)
        filter.addAction(PUPyConstants.ACTION_LOCAL_SEND_UNFAMILIARITY)
        filter.addAction(PUPyConstants.ACTION_LOCAL_SEND_PROXIMITY)
        LocalBroadcastManager.getInstance(this).registerReceiver(localDataReceiver, filter)
    }

    override fun onCreate() {
        bindToFramework()
        setupLocalDataReceiver()
        Log.d(logTag, javaClass.name + " created")
    }

    override fun onDestroy() {
        unbindFromFramework()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localDataReceiver)
        Log.d(logTag, javaClass.name + " destroyed")
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Binding is not supported")
    }

    private val localDataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = Bundle()
            bundle.putParcelable(PUPyConstants.KEY_COMPONENT_NAME, serviceComponentName)
            if (PUPyConstants.ACTION_LOCAL_SEND_PRIVACY == intent.action) {
                val statusDataPrivacy: StatusDataPrivacy = intent.getParcelableExtra(PUPyConstants.KEY_STATUS_DATA_PRIVACY)!!
                bundle.putParcelable(PUPyConstants.KEY_STATUS_DATA_PRIVACY, statusDataPrivacy)
            } else if (PUPyConstants.ACTION_LOCAL_SEND_UNFAMILIARITY == intent.action) {
                val statusDataUnfamiliarity: StatusDataUnfamiliarity = intent.getParcelableExtra(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY)!!
                bundle.putParcelable(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY, statusDataUnfamiliarity)
            } else if (PUPyConstants.ACTION_LOCAL_SEND_PROXIMITY == intent.action) {
                val statusDataProximity: StatusDataProximity = intent.getParcelableExtra(PUPyConstants.KEY_STATUS_DATA_PROXIMITY)!!
                bundle.putParcelable(PUPyConstants.KEY_STATUS_DATA_PROXIMITY, statusDataProximity)
            }

            publishDataUpdate(bundle)
        }
    }

    companion object {
        val LOG_TAG = AbstractPluginService::class.java.simpleName
    }
}