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
package ca.uwaterloo.mrafuse.context.framework.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Created by fhdwsse
 */
object PermissionHelper {
    fun checkAndGetPermissions(activity: Activity?, vararg permissions: String) {
        val permissionsToAsk: MutableList<String> = LinkedList()
        for (eachPermission in permissions) {
            if (ContextCompat.checkSelfPermission(activity!!, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAsk.add(eachPermission)
            }
        }
        if (permissionsToAsk.isEmpty()) return
        //TODO React to user feedback and don't pretend he will always click yes.
        ActivityCompat.requestPermissions(activity!!, permissionsToAsk.toTypedArray(), 1)
    }

    fun hasPermission(context: Context, permission: String?): Boolean {
        val packageManager = context.packageManager
        return packageManager.checkPermission(permission,
                context.packageName) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPermissionAny(context: Context, permissions: Array<String>): Boolean {
        return permissions.any() { hasPermission(context, it)}
    }
}