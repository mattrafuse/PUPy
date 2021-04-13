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
package ca.uwaterloo.mrafuse.bluetoothplugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.AbstractUnfamiliarityService
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity
import ca.uwaterloo.mrafuse.bluetoothplugin.common.Database
import java.lang.StringBuilder
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt

class UnfamiliarityPluginService : AbstractUnfamiliarityService() {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val database: Database = Database(this)

    private val devices = hashSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "In OnCreate")

        createNotificationChannel("default")
        startForeground(43796852, buildForegroundNotification("default"))

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter!!.isDiscovering) {
            Log.i(LOG_TAG, "Starting Discovery")
            mBluetoothAdapter!!.startDiscovery()
        }

        mBluetoothAdapter!!.bluetoothLeScanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.scanRecord?.manufacturerSpecificData?.let { record ->
                    val address = result.device.address
                    if (devices.contains(address) || !database.hasAddress(address)) {
                        return@let
                    } else {
                        devices.add(address)
                    }

                    Log.i(LOG_TAG, record.size().toString())

                    val manufacturers = (0 until record.size())
                            .map { record.keyAt(it).toString() }
                    Log.d(LOG_TAG, "Manufacturers detected for $address: " + manufacturers.joinToString { it.toString() })

                    val shouldIgnore = manufacturers.any { ignoredManufacturers.contains(it) }
                    if (shouldIgnore) {
                        database.ignoreAddress(address)
                    }
                }
                super.onScanResult(callbackType, result)
            }
        })

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(mReceiver)
    }

    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address

                    Log.d(LOG_TAG, "Found Device: $deviceName, $deviceHardwareAddress")

                    database.addAddress(deviceHardwareAddress)
                }
            }
        }
    }

    private fun createNotificationChannel(id: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
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

    private fun buildForegroundNotification(channel: String): Notification {
        val b = Notification.Builder(this, channel)
        b.setOngoing(true)
                .setContentTitle("PUPy")
                .setContentText("BluetoothUnfamiliarityService")
                .setSmallIcon(android.R.drawable.star_on)
                .setTicker("PUPy")
        return b.build()
    }

    override fun onDataUpdateRequest() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter!!.isEnabled) {
                Log.e(LOG_TAG, "Bluetooth not enabled.")
                publishUnfamiliarityUpdate(StatusDataUnfamiliarity()
                        .status(StatusDataUnfamiliarity.Status.UNKNOWN)
                        .unfamiliarity(0))
                return
            }

            if (!mBluetoothAdapter!!.isDiscovering) {
                Log.i(LOG_TAG, "Starting Discovery")
                mBluetoothAdapter!!.startDiscovery()
            }
        } else {
            Log.e(LOG_TAG, "Bluetooth adapter is null")
        }

        val time = System.currentTimeMillis()
        val addresses = database.getAddresses()
                .sortedBy { it.seen }
                .groupBy { it.address }
        val learningRate = 0.05f
        // 10 minute intervals
        val interval = 1000 * 60 * 10L
        val presentDevices = addresses.values
                .filter { sightings -> sightings.any {time - it.seen < interval} }

        Log.i(LOG_TAG, presentDevices.size.toString())

        val contextFamiliarity = presentDevices
                .map { sightings ->
                    val intervals = sightings.last().seen..time  step interval
                    intervals.map { timestamp -> sightings.any { sighting ->
                        timestamp <= sighting.seen && sighting.seen < timestamp + interval }
                    }.fold(0.0f, {acc, hit ->
                        learningRate * (if (hit) 1.0f else acc) + (1 - learningRate) * acc
                    })
                }
                .sum()

        publishUnfamiliarityUpdate(StatusDataUnfamiliarity()
                .status(StatusDataUnfamiliarity.Status.OPERATIONAL)
                .unfamiliarity((presentDevices.size * (1.0f - contextFamiliarity)).roundToInt()))
    }

    companion object {
        val ignoredManufacturers = arrayOf(
                "76",  // Apple
                "224", // Google
                "369"  // Amazon
        )
        private val LOG_TAG = UnfamiliarityPluginService::class.java.simpleName
    }
}