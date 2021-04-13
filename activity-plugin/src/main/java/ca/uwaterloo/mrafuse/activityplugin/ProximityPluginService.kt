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
package ca.uwaterloo.mrafuse.activityplugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ca.uwaterloo.mrafuse.context.api.AbstractProximityService
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import com.google.android.gms.location.*

class ProximityPluginService : AbstractProximityService() {
    var broadcastReceiver: BroadcastReceiver? = null
    var type: Int? = null
    var confidence: Int? = null
    var requestUpdatesSucceeded = false

    private var mIntentService: Intent? = null
    private var mPendingIntent: PendingIntent? = null
    private var mActivityRecognitionClient: ActivityRecognitionClient? = null
    var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: ProximityPluginService
            get() = this@ProximityPluginService
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(678342, buildForegroundNotification())

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Parameters.RECEIVE_DETECTED_ACTIVITY) {
                    type = intent.getIntExtra("type", -1)
                    confidence = intent.getIntExtra("confidence", 0)
                }
            }
        }

        mActivityRecognitionClient = ActivityRecognitionClient(this)
        mIntentService = Intent(this, ProximityPluginService::class.java)
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService!!, PendingIntent.FLAG_UPDATE_CURRENT)
        requestActivityUpdatesHandler()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val detectedActivities = result.probableActivities
            for (activity in detectedActivities) {
                Log.d(LOG_TAG, "Detected activity: " + activity.type + ", " + activity.confidence)
            }

            val mainResult = detectedActivities.maxBy { it.confidence }
            type = mainResult!!.type
            confidence = mainResult.confidence
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver!!)

        removeActivityUpdatesHandler()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private fun createNotificationChannel() {
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

    private fun buildForegroundNotification(channel: String = "default"): Notification {
        val b = NotificationCompat.Builder(this, channel)
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setContentText("ActivityProximityService")
                .setSmallIcon(android.R.drawable.star_on)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setTicker("PUPy")
        return b.build()
    }

    override fun onDataUpdateRequest() {
        requestActivityUpdatesHandler()

        var result = 20
        when (type) {
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_FOOT,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING -> {
                result = 0
            }
            DetectedActivity.IN_VEHICLE -> {
                result = 2
            }
            DetectedActivity.STILL -> {
                result = 5
            }
            null -> {
                result = -1
            }
        }

        Log.d(LOG_TAG, "$type $result")

        if (result >= 0) {
            publishRiskUpdate(StatusDataProximity()
                    .status(StatusDataProximity.Status.OPERATIONAL)
                    .proximity(result))
        } else {
            publishRiskUpdate(StatusDataProximity()
                    .status(StatusDataProximity.Status.UNKNOWN)
                    .proximity(0))
        }
    }

    fun requestActivityUpdatesHandler() {
        if (!requestUpdatesSucceeded) {
            val task = mActivityRecognitionClient!!.requestActivityUpdates(
                    Parameters.DETECTION_INTERVAL_IN_MILLISECONDS,
                    mPendingIntent)
            task.addOnSuccessListener() {
                Log.d(LOG_TAG, "Successfully requested activity updates")
                requestUpdatesSucceeded = true
            }

            task.addOnFailureListener() {
                Log.e(LOG_TAG, "Requesting activity updates failed to start")
            }
        }
    }

    fun removeActivityUpdatesHandler() {
        val task = mActivityRecognitionClient!!.removeActivityUpdates(mPendingIntent)
        task.addOnSuccessListener() {
            Log.d(LOG_TAG, "Removed activity updates successfully")
            requestUpdatesSucceeded = false
        }

        task.addOnFailureListener() {
            Log.e(LOG_TAG, "Failed to remove activity updates")
            requestUpdatesSucceeded = false
        }
    }

    companion object {
        val LOG_TAG = ProximityPluginService::class.java.simpleName
    }
}