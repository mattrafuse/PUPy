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
package ca.uwaterloo.mrafuse.randomplugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.AbstractPrivacyService
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy

class PrivacyPluginService : AbstractPrivacyService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("RandomConfidenceService", "In OnCreate")

        createNotificationChannel("default")
        startForeground(43796892, buildForegroundNotification("default"))
    }

    private fun createNotificationChannel(id: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Default"
            val description = "All PUPy Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            channel.importance = NotificationManager.IMPORTANCE_LOW
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(channel: String): Notification {
        val b = Notification.Builder(this, channel)
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setContentText("RandomPrivacyService")
                .setSmallIcon(android.R.drawable.star_on)
                .setTicker("PUPy")
        return b.build()
    }

    override fun onDataUpdateRequest() {
        publishPrivacyUpdate(StatusDataPrivacy()
                .status(StatusDataPrivacy.Status.OPERATIONAL)
                .privacy(arrayOf((0..50), (0..15), (0..15)).random().random()))
    }
}