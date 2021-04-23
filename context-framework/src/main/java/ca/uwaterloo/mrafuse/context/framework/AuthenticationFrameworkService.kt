/**
 * Copyright 2020 - 2021
 *
 * Matthew Rafuse <matthew.rafuse@uwaterloo.ca>
 *
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
@file:Suppress("SameParameterValue")

package ca.uwaterloo.mrafuse.context.framework

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.PUPyConstants
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.api.PermissionUtil.checkRegisterPermission
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity
import ca.uwaterloo.mrafuse.context.framework.common.TypedServiceConnection
import ca.uwaterloo.mrafuse.context.framework.lock.LockService
import ca.uwaterloo.mrafuse.context.framework.module.RuleModule
import ca.uwaterloo.mrafuse.context.framework.module.strategies.AggregatorStrategyPrivacyDefault
import ca.uwaterloo.mrafuse.context.framework.module.strategies.AggregatorStrategyProximityDefault
import ca.uwaterloo.mrafuse.context.framework.module.strategies.AggregatorStrategyUnfamiliarityDefault
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginInfo
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager

/**
 * The core service of the AuthenticationFramework.
 * Starts all available plugins on startup (newly installed plugins are started by the PluginPackageChangeReceiver).
 * Plugins bind to this service and communicate through a Messenger.
 * If this service dies, all bound plugins will automatically shutdown.
 *
 * Plugins need the following requirements in order to connect to the AuthenticationFramework:
 * (see confidenceplugin for example usage)
 *
 * Permissions: at.usmile.cormorant.REGISTER_AUTH_PLUGIN
 * Manifest Meta Data:
 * - Plugin Service:
 * -> apiVersion
 * -> pluginType [risk, confidence]
 * -> title
 * -> description
 * -> configurationActivity (optional, SimpleName of the activity for configuration purposes)
 * -> explicitAuthActivity (optional, SimpleName of the activity for explicit authentication)
 * -> implicit [true, false]
 * - Application
 * -> startupService (SimpleName of the plugin service)
 */
class AuthenticationFrameworkService : Service() {
    private val pluginManager: PluginManager = PluginManager.instance!!
    private var ruleModule: RuleModule? = null
    private val lockService: TypedServiceConnection<LockService?> = TypedServiceConnection()
    private val boundService = AuthenticationFrameworkServiceBinder()

    internal inner class PluginMessageHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i(LOG_TAG, "handleMessage ${msg.what}")
            when (msg.what) {
                PUPyConstants.MSG_ADD_PLUGIN -> registerPlugin(msg)
                PUPyConstants.MSG_PRIVACY -> readData(PLUGIN_TYPE.privacy, msg)
                PUPyConstants.MSG_UNFAMILIARITY -> readData(PLUGIN_TYPE.unfamiliarity, msg)
                PUPyConstants.MSG_PROXIMITY -> readData(PLUGIN_TYPE.proximity, msg)
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun registerPlugin(msg: Message) {
        Log.i(LOG_TAG, "registerPlugin")
        val pluginComponentName = msg.data.getParcelable<ComponentName>(PUPyConstants.KEY_COMPONENT_NAME)
        Log.d(LOG_TAG, pluginComponentName.toString())
        val api = PluginInfo(msg.replyTo)
        setPluginMetaData(api, pluginComponentName)
        if (pluginComponentName == null || !checkRegisterPermission(LOG_TAG, this@AuthenticationFrameworkService, pluginComponentName.packageName)) {
            return
        }
        pluginManager.addPlugin(api)
    }

    private fun readData(type: PLUGIN_TYPE, msg: Message) {
        val dataBundle = msg.data
        dataBundle.classLoader = PUPyConstants::class.java.classLoader
        val pluginComponent = dataBundle.getParcelable<ComponentName>(PUPyConstants.KEY_COMPONENT_NAME)
        if (pluginComponent == null || !checkRegisterPermission(LOG_TAG, this@AuthenticationFrameworkService, pluginComponent.packageName)) {
            return
        }
        Log.d(LOG_TAG, pluginComponent.className + " " + type.toString())
        if (PLUGIN_TYPE.privacy == type) {
            val statusDataPrivacy: StatusDataPrivacy = dataBundle.getParcelable(PUPyConstants.KEY_STATUS_DATA_PRIVACY)!!
            pluginManager.changePrivacy(pluginComponent.className, statusDataPrivacy)
        } else if (PLUGIN_TYPE.unfamiliarity == type) {
            val statusDataUnfamiliarity: StatusDataUnfamiliarity = msg.data.getParcelable(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY)!!
            pluginManager.changeUnfamiliarity(pluginComponent.className, statusDataUnfamiliarity)
        } else if (PLUGIN_TYPE.proximity == type) {
            val statusDataProximity: StatusDataProximity = msg.data.getParcelable(PUPyConstants.KEY_STATUS_DATA_PROXIMITY)!!
            pluginManager.changeProximity(pluginComponent.className, statusDataProximity)
        }
    }

    val messenger = Messenger(PluginMessageHandler())
    private fun initDecisionModule() {
        //Choose module strategies
        ruleModule = RuleModule(
                this,
                AggregatorStrategyPrivacyDefault(),
                AggregatorStrategyUnfamiliarityDefault(),
                AggregatorStrategyProximityDefault())
        PluginManager.instance!!.addPluginChangeListener(ruleModule!!)
        ruleModule!!.start()
    }

    override fun onCreate() {
        Log.d(LOG_TAG, "AuthenticationFrameworkService started")
        reconnectAllPlugins()
        initDecisionModule()
        bindService(Intent(this, LockService::class.java), lockService, Context.BIND_AUTO_CREATE)
        createDefaultNotificationChannel()
        startForeground(5768, buildForegroundNotification("default"))
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "AuthenticationFrameworkService stopped")
        ruleModule!!.stop()
        if (lockService.isBound) unbindService(lockService)

        for (plugin in pluginManager.pluginListReadOnly) {
            val intent = Intent()
            intent.component = plugin.componentName
            stopService(intent)
            pluginManager.removePlugin(plugin.componentName!!.className)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(LOG_TAG, "onBind $intent")

        return messenger.binder
    }

    //TODO Handle null and invalid meta values
    private fun setPluginMetaData(api: PluginInfo, componentName: ComponentName?) {
        try {
            val serviceInfo = packageManager.getServiceInfo(componentName!!, PackageManager.GET_META_DATA)
            val metaData = serviceInfo.metaData
            api.componentName = componentName
            api.configurationComponentName = createComponentName(componentName, metaData.getString(PUPyConstants.META_CONFIGURATION))
            api.explicitAuthComponentName = createComponentName(componentName, metaData.getString(PUPyConstants.META_EXPLICIT_AUTH))
            api.pluginType = PLUGIN_TYPE.valueOf(metaData.getString(PUPyConstants.META_PLUGIN_TYPE)!!)
            api.title = metaData.getString(PUPyConstants.META_TITLE)
            api.apiVersion = metaData.getInt(PUPyConstants.META_API_VERSION)
            api.description = metaData.getString(PUPyConstants.META_DESCRIPTION)
            api.isImplicit = metaData.getBoolean(PUPyConstants.META_IMPLICIT)
            api.icon = packageManager.getApplicationIcon(componentName.packageName)
            api.statusDataPrivacy = StatusDataPrivacy()
            api.statusDataUnfamiliarity = StatusDataUnfamiliarity()
            api.statusDataProximity = StatusDataProximity()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(LOG_TAG, "Failed to load meta-data, NameNotFound: " + e.message)
        }
    }

    private fun createDefaultNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name: CharSequence = "Default"
        val description = "All PUPy Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("default", name, importance)
        channel.description = description
        channel.importance = NotificationManager.IMPORTANCE_LOW
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(channel: String): Notification {
        val b = Notification.Builder(this, channel)
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setContentText("PUPy is running")
                .setSmallIcon(R.drawable.star_on)
                .setTicker("PUPy")
        return b.build()
    }

    private fun createComponentName(mainComponent: ComponentName?, className: String?): ComponentName? {
        return if (className == null || className == "") null else ComponentName(mainComponent!!.packageName, mainComponent.packageName + "." + className)
    }

    private fun reconnectAllPlugins() {
        Log.d(LOG_TAG, "reconnectAllPlugins")
        ReconnectPluginTask(packageManager).execute()
    }

    @SuppressLint("StaticFieldLeak")
    inner class ReconnectPluginTask(val packageManager: PackageManager) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void) {
            val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            for (eachInstalledPackage in installedPackages) {
                val packageName = eachInstalledPackage.packageName
                val metaData = eachInstalledPackage.applicationInfo.metaData ?: continue
                val startupServiceName = metaData.getString(PUPyConstants.META_STARTUP_SERVICE) ?: continue

                for (service in startupServiceName.split(",")) {
                    val serviceComponent = ComponentName(packageName, "$packageName.$service")
                    Log.i(LOG_TAG, "Starting " + serviceComponent.className)
                    val serviceIntent = Intent()
                    serviceIntent.component = serviceComponent
                    this@AuthenticationFrameworkService.startForegroundService(serviceIntent)
                }
            }
            return
        }
    }

    inner class AuthenticationFrameworkServiceBinder : Binder() {
        fun getService() : AuthenticationFrameworkService {
            return this@AuthenticationFrameworkService
        }
    }

    companion object {
        private val LOG_TAG = AuthenticationFrameworkService::class.java.simpleName
    }
}