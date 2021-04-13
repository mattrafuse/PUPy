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
package ca.uwaterloo.mrafuse.locationplugin

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.AbstractUnfamiliarityService
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity
import ca.uwaterloo.mrafuse.locationplugin.common.Database
import ca.uwaterloo.mrafuse.locationplugin.common.PermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

class UnfamiliarityPluginService : AbstractUnfamiliarityService() {
    private val database = Database.getInstance(this)
    private val boundService = UnfamiliarityPluginServiceBinder()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel("default")
        startForeground(437812, buildForegroundNotification("default"))
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder {
        return boundService
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
                .setContentText("LocationUnfamiliarityService")
                .setSmallIcon(android.R.drawable.star_on)
                .setTicker("PUPy")
        return b.build()
    }


    @SuppressLint("MissingPermission")
    override fun onDataUpdateRequest() {
        val perms = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (PermissionHelper.hasPermissionAny(applicationContext, perms)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { loc ->
                val unfamiliarity = if (loc != null) {
                    Log.d(PrivacyPluginService.LOG_TAG, "Loc is not null")
                    val shortestDistance = database.getLocations().map {
                        val results = FloatArray(1)
                        Location.distanceBetween(loc.latitude, loc.longitude, it.latitude, it.longitude, results)
                        results[0]
                    }.min()

                    val distance = ((shortestDistance ?: Float.MAX_VALUE).toDouble() - Parameters.THRESHOLD)
                            .coerceAtLeast(0.0)
                    (distance / 75.0).roundToInt()
                } else {
                    Log.d(PrivacyPluginService.LOG_TAG, "Loc is null")
                    -1
                }

                Log.i(LOG_TAG, "onDataUpdateRequest - unfamiliarity $unfamiliarity")

                if (unfamiliarity >= 0) {
                    publishUnfamiliarityUpdate(StatusDataUnfamiliarity()
                            .status(StatusDataUnfamiliarity.Status.OPERATIONAL)
                            .unfamiliarity(unfamiliarity))
                } else {
                    publishUnfamiliarityUpdate(StatusDataUnfamiliarity()
                            .status(StatusDataUnfamiliarity.Status.UNKNOWN)
                            .unfamiliarity(0))
                }
            }
        }

    }

    inner class UnfamiliarityPluginServiceBinder : Binder() {
        fun getService() : UnfamiliarityPluginService {
            return this@UnfamiliarityPluginService
        }
    }

    companion object {
        val LOG_TAG = UnfamiliarityPluginService::class.java.toString()
    }
}