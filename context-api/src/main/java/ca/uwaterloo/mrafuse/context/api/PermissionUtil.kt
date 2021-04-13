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

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import android.util.Log
import android.widget.Toast

object PermissionUtil {
    private const val KEY_PERMISSIONS = "permissions"
    private const val KEY_GRANT_RESULTS = "grantResults"
    private const val KEY_RESULT_RECEIVER = "resultReceiver"
    private const val KEY_REQUEST_CODE = "requestCode"
    private const val NOTIFICATION_INTENT_REQUEST = 1
    @JvmStatic
    fun checkRegisterPermission(logTag: String, context: Context, packageName: String): Boolean {
        return checkForPermission(logTag, context, packageName, Permissions.Companion.REGISTER_AUTH_PLUGIN)
    }

    fun checkReadPluginDataPermission(logTag: String, context: Context, packageName: String): Boolean {
        return checkForPermission(logTag, context, packageName, Permissions.Companion.READ_PLUGIN_DATA)
    }

    private fun checkForPermission(logTag: String, context: Context, packageName: String, permission: String): Boolean {
        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            if (packageInfo.requestedPermissions != null) {
                for (eachRequestPermission in packageInfo.requestedPermissions) {
                    if (permission == eachRequestPermission) {
                        return true
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(logTag, "Failed to load permissions, NameNotFound: " + e.message)
        }
        Log.d(logTag, "Plugin $packageName has not the required permissions to connect to the Authentication Framework")
        Toast.makeText(context, "$packageName has not the required permissions to connect to the Authentication Framework", Toast.LENGTH_LONG).show()
        return false
    }

    @JvmStatic
    fun requestPermissions(context: Context, callback: OnRequestPermissionsResultCallback, requestCode: Int, notificationIcon: Int, vararg permissions: String?) {
        val resultReceiver: ResultReceiver = object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                val outPermissions = resultData.getStringArray(KEY_PERMISSIONS)
                val grantResults = resultData.getIntArray(KEY_GRANT_RESULTS)
                callback.onRequestPermissionsResult(resultCode, outPermissions!!, grantResults!!)
            }
        }
        val permIntent = Intent(context, PermissionRequestActivity::class.java)
        permIntent.putExtra(KEY_RESULT_RECEIVER, resultReceiver)
        permIntent.putExtra(KEY_PERMISSIONS, permissions)
        permIntent.putExtra(KEY_REQUEST_CODE, requestCode)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(permIntent)
        val permPendingIntent = stackBuilder.getPendingIntent(
                NOTIFICATION_INTENT_REQUEST,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, "default")
                .setSmallIcon(notificationIcon)
                .setContentTitle("Additional permissions required")
                .setContentText("Tap to manage permissions")
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setWhen(0)
                .setContentIntent(permPendingIntent)
                .setStyle(null)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, builder.build())
    }

    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
    }

    class PermissionRequestActivity : Activity() {
        lateinit var resultReceiver: ResultReceiver
        lateinit var permissions: Array<String>
        var requestCode = 0
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            val resultData = Bundle()
            resultData.putStringArray(KEY_PERMISSIONS, permissions)
            resultData.putIntArray(KEY_GRANT_RESULTS, grantResults)
            resultReceiver.send(requestCode, resultData)
            finish()
        }

        override fun onStart() {
            super.onStart()
            resultReceiver = this.intent.getParcelableExtra(KEY_RESULT_RECEIVER)!!
            permissions = this.intent.getStringArrayExtra(KEY_PERMISSIONS)!!
            requestCode = this.intent.getIntExtra(KEY_REQUEST_CODE, 0)
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
    }
}