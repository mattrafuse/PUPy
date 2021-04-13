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
package ca.uwaterloo.mrafuse.functionalityplugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.AbstractFunctionalityService
import ca.uwaterloo.mrafuse.context.api.PUPyConstants

class FunctionalityPluginService : AbstractFunctionalityService() {
    private lateinit var mIntentService: Intent
    private lateinit var mPendingIntent: PendingIntent

    private var isPlaying = false
    private var oldVolume = 0
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()

        createDefaultNotificationChannel()
        createAlertNotificationChannel()
        updateNotification("Not Initialized", "Not Initialized", "Not Initialized")

        mIntentService = Intent(this, FunctionalityPluginService::class.java)
                .apply { action = "functionalityModuleStopAlarm" }
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT)

        val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer()
                .apply {
                    setDataSource(applicationContext, alert)
                    setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                "functionalityModuleStopAlarm" -> {
                    val audioManager = getSystemService(AudioManager::class.java)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldVolume, 0)
                    mediaPlayer.stop()
                    isPlaying = false
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(true)
    }

    private fun updateNotification(authStatus: String, theftStatus: String, lossStatus: String) {
        val b = Notification.Builder(this, "default")
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setStyle(Notification.BigTextStyle().bigText("Authentication: $authStatus\nTheft: $theftStatus\nLoss: $lossStatus"))
                .setSmallIcon(android.R.drawable.star_on)
                .setTicker("PUPy")

        startForeground(43796892, b.build())
    }

    private fun createDefaultNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name: CharSequence = "Functionality Plugin"
        val description = "All PUPy Notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("default", name, importance)
        channel.description = description
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun createAlertNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name: CharSequence = "Alert"
        val description = "Alerts from rules"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("alert", name, importance)
        channel.description = description
        channel.importance = NotificationManager.IMPORTANCE_HIGH
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }

    override fun onNewPublishedData(msg: Message) {
        val privacy = msg.data.getDouble(PUPyConstants.KEY_STATUS_DATA_PRIVACY)
        val unfamiliarity = msg.data.getDouble(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY)
        val proximity = msg.data.getDouble(PUPyConstants.KEY_STATUS_DATA_PROXIMITY)

        updateNotification(
                decideAuthentication(privacy, unfamiliarity),
                decideTheft(unfamiliarity, proximity),
                decideLoss(privacy, proximity)
        )
    }

    private fun decideAuthentication(privacy: Double = 0.0, unfamiliarity: Double = 0.0): String {
        return when  {
            privacy - unfamiliarity < -.4 -> "High Alert"
            privacy - unfamiliarity < .1 -> "Enabled"
            else -> "Disabled"
        }
    }

    private fun playAlarmSound() {
        if (isPlaying) return
        Log.i(LOG_TAG, "Playing Alarm Sound")

        val audioManager = getSystemService(AudioManager::class.java)
        oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)

        mediaPlayer.prepare()
        mediaPlayer.start()
        isPlaying = true


        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        val b = Notification.Builder(this, "default")
        b.setContentTitle("Device Being Stolen")
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setChannelId("alert")
                .setTicker("PUPy")
                .setContentIntent(mPendingIntent)
                .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), b.build())
    }

    private var theftEnabled = false
    private fun decideTheft(unfamiliarity: Double = 0.0, proximity: Double = 0.0): String {
        if (unfamiliarity > .5 && proximity <= .2) {
            playAlarmSound()
        }

        return when {
            unfamiliarity > .5 && proximity < .9 -> {
                theftEnabled = true
                "Enabled"
            }
            unfamiliarity <= .1 || proximity >= 1.0 -> {
                theftEnabled = false
                "Disabled"
            }
            theftEnabled -> "Enabled"
            else -> "Disabled"
        }
    }

    private fun sendDeviceLostNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        val b = Notification.Builder(this, "default")
        b.setContentTitle("Device Lost")
                .setContentText("Device is potentially lost")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setChannelId("alert")
                .setTicker("PUPy")

        notificationManager.notify(59384, b.build())
    }

    private var lossEnabled = false
    private fun decideLoss(privacy: Double = 0.0, proximity: Double = 0.0): String {
        if (proximity < .3 && privacy < .5) {
            sendDeviceLostNotification()
        }

        return when {
            proximity <= .75 -> {
                lossEnabled = true
                "Enabled"
            }
            proximity > .85 -> {
                lossEnabled = false
                "Disabled"
            }
            lossEnabled -> "Enabled"
            else -> "Disabled"
        }
    }

    companion object {
        val LOG_TAG = FunctionalityPluginService::class.java.simpleName
    }
}