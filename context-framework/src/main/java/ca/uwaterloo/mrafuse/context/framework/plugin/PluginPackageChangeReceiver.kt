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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.PUPyConstants
import ca.uwaterloo.mrafuse.context.api.PermissionUtil.checkRegisterPermission

/**
 * Removes plugins if they are uninstalled and starts plugins if they are installed.
 */
//TODO package update (=replace)? (for non adb updates) - react to compontent changes (start/stop =change)
class PluginPackageChangeReceiver : BroadcastReceiver() {
    private val pluginManager: PluginManager?
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data!!.schemeSpecificPart
        Log.d(LOG_TAG, "Change: action: " + intent.action + " package: " + packageName)
        if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
            pluginManager!!.removePlugin(packageName)
        }
        if (Intent.ACTION_PACKAGE_ADDED == intent.action) {
            val serviceComponent = getStartupServiceComponent(context, packageName)
            if (serviceComponent != null) {
                Log.d(LOG_TAG, "Starting " + serviceComponent.shortClassName)
                if (!checkRegisterPermission(LOG_TAG, context, packageName)) return
                val serviceIntent = Intent()
                serviceIntent.component = serviceComponent
                context.startService(serviceIntent)
            }
        }
    }

    private fun getStartupServiceComponent(context: Context, packageName: String): ComponentName? {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA + PackageManager.GET_PERMISSIONS)
            val metaData = applicationInfo.metaData
            if (metaData == null) {
                Log.d(LOG_TAG, "MetaData: " + PUPyConstants.META_STARTUP_SERVICE + " not found for: " + packageName)
                return null
            }
            val startupServiceName = metaData.getString(PUPyConstants.META_STARTUP_SERVICE)
            if (startupServiceName == null || "" == startupServiceName) {
                Log.d(LOG_TAG, "MetaData: " + PUPyConstants.META_STARTUP_SERVICE + " not found for: " + packageName)
                return null
            }
            return ComponentName(packageName, "$packageName.$startupServiceName")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(LOG_TAG, "getStartupServiceComponent failed - nameNotFound:" + e.message)
        }
        return null
    }

    companion object {
        val LOG_TAG = PluginPackageChangeReceiver::class.java.simpleName
    }

    init {
        pluginManager = PluginManager.instance
    }
}