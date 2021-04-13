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
package ca.uwaterloo.mrafuse.proximityplugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.uwaterloo.mrafuse.context.api.AbstractProximityService
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import kotlin.math.roundToInt

class ProximityPluginService : AbstractProximityService(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var mProximity: Sensor
    private var distance = 0.0f

    override fun onCreate() {
        super.onCreate()

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)

        createNotificationChannel("default")
        startForeground(987348, buildForegroundNotification("default"))
    }

    override fun onDestroy() {
        super.onDestroy()

        mSensorManager.unregisterListener(this)
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
        val b = NotificationCompat.Builder(this, channel)
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setContentText("ProximityPluginService")
                .setSmallIcon(android.R.drawable.star_on)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setTicker("PUPy")
        return b.build()
    }

    override fun onDataUpdateRequest() {
        publishRiskUpdate(StatusDataProximity()
                .status(StatusDataProximity.Status.OPERATIONAL)
                .proximity(distance.roundToInt()))

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            distance = if (event.values[0] < 1) {
                0.0f
            } else {
                4.0f
            }
        }
    }

    companion object {
        private val LOG_TAG = ProximityPluginService::class.java.simpleName
    }
}