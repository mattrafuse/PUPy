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
package ca.uwaterloo.mrafuse.context.framework.plugin

import android.os.Bundle
import android.os.DeadObjectException
import android.os.Message
import android.os.RemoteException
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.PUPyConstants
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity
import java.util.*

/**
 * Manages the list of active plugins and caches their data.
 * Informs other classes about plugin changes, if they implement the OnChangeListener
 */
class PluginManager {
    private val pluginChangeListeners: MutableList<PluginChangeListener> = LinkedList()
    private val pluginList: MutableList<PluginInfo> = ArrayList() //TODO persistence required?
    fun getPluginInfo(className: String): PluginInfo? {
        for (eachPluginInfo in pluginList) {
            if (eachPluginInfo.componentName!!.className == className) {
                return eachPluginInfo
            }
        }
        return null
    }

    fun changePrivacy(packageName: String, statusDataPrivacy: StatusDataPrivacy) {
        val api = getPluginInfo(packageName) ?:  return
        api.statusDataPrivacy = statusDataPrivacy
        api.lastUpdate = Calendar.getInstance()
        notifyOnChangeListeners()
        Log.d(LOG_TAG, "Confidence for " + api.title + " changed to " + statusDataPrivacy.privacy)
    }

    fun changeUnfamiliarity(packageName: String, statusDataUnfamiliarity: StatusDataUnfamiliarity) {
        val api = getPluginInfo(packageName) ?: return
        api.statusDataUnfamiliarity = statusDataUnfamiliarity
        api.lastUpdate = Calendar.getInstance()
        notifyOnChangeListeners()
        Log.d(LOG_TAG, "Risk for " + api.title + " changed to " + statusDataUnfamiliarity.unfamiliarity)
    }

    fun changeProximity(packageName: String, statusDataProximity: StatusDataProximity) {
        val api = getPluginInfo(packageName) ?: return
        api.statusDataProximity = statusDataProximity
        api.lastUpdate = Calendar.getInstance()
        notifyOnChangeListeners()
        Log.d(LOG_TAG, "Risk for " + api.title + " changed to " + statusDataProximity.proximity)
    }

    fun removePlugin(packageName: String) {
        pluginList.remove(getPluginInfo(packageName))
        notifyOnChangeListeners()
        Log.d(LOG_TAG, "PluginList size after remove: " + pluginList.size)
    }

    fun addPlugin(pluginInfo: PluginInfo) {
        Log.i(LOG_TAG, "addPlugin")
        pluginList.add(pluginInfo)
        notifyOnChangeListeners()
        Log.d(LOG_TAG, "PluginList size after add: " + pluginList.size)
    }

    val pluginListReadOnly: List<PluginInfo>
        get() = Collections.unmodifiableList(pluginList)

    fun addPluginChangeListener(listener: PluginChangeListener) {
        pluginChangeListeners.add(listener)
    }

    fun removePluginChangeListener(listener: PluginChangeListener?) {
        pluginChangeListeners.remove(listener)
    }

    fun pollDataFromPlugins() {
        val inputModules = instance!!.pluginListReadOnly.filter {
            it.pluginType in arrayOf(
                    PUPyConstants.PLUGIN_TYPE.privacy,
                    PUPyConstants.PLUGIN_TYPE.unfamiliarity,
                    PUPyConstants.PLUGIN_TYPE.proximity
            )
        }

        Log.v(LOG_TAG, "Requesting data from plugins: ${inputModules.size}")
        for (eachPluginInfo in inputModules) {
            val msg = Message.obtain(null, PUPyConstants.MSG_POLL_DATA)
            try {
                Log.v(LOG_TAG, eachPluginInfo.title!!)
                eachPluginInfo.messenger!!.send(msg)
            } catch (e: DeadObjectException) {
                Log.e(LOG_TAG, "Lost connection to " + eachPluginInfo.title, e)
                instance!!.removePlugin(eachPluginInfo.componentName!!.className)
            } catch (e: RemoteException) {
                Log.e(LOG_TAG, "Can't request data from " + eachPluginInfo.title, e)
            }
        }
    }

    fun postDataToPlugins(privacy: Double, unfamiliarity: Double, proximity: Double) {
        val functionalityModules = instance!!.pluginListReadOnly.filter {
            it.pluginType == PUPyConstants.PLUGIN_TYPE.functionality
        }

        Log.v(LOG_TAG, "Publishing data to plugins: ${functionalityModules.size}")
        for (eachPluginInfo in functionalityModules) {
            val msg = Message.obtain(null, PUPyConstants.MSG_PUBLISH_DATA)
            val msgData = Bundle()
            msgData.putDouble(PUPyConstants.KEY_STATUS_DATA_PRIVACY, privacy)
            msgData.putDouble(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY, unfamiliarity)
            msgData.putDouble(PUPyConstants.KEY_STATUS_DATA_PROXIMITY, proximity)
            msg.data = msgData
            try {
                Log.v(LOG_TAG, eachPluginInfo.title!!)
                eachPluginInfo.messenger!!.send(msg)
            } catch (e: DeadObjectException) {
                Log.e(LOG_TAG, "Lost connection to " + eachPluginInfo.title, e)
                instance!!.removePlugin(eachPluginInfo.componentName!!.className)
            } catch (e: RemoteException) {
                Log.e(LOG_TAG, "Can't request data from " + eachPluginInfo.title, e)
            }
        }
    }

    private fun notifyOnChangeListeners() {
        Log.i(LOG_TAG, "notifyOnChangeListeners")
        for (eachPluginChangeListener in pluginChangeListeners) {
            eachPluginChangeListener.onPluginsChanged()
        }
    }

    interface PluginChangeListener {
        fun onPluginsChanged()
    }

    companion object {
        private val LOG_TAG = PluginManager::class.java.simpleName
        var instance: PluginManager? = null
            get() {
                if (field == null) {
                    field = PluginManager()
                }
                return field
            }
            private set
    }
}