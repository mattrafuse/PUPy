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
package ca.uwaterloo.mrafuse.context.framework.lock

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import ca.uwaterloo.mrafuse.context.framework.R
import ca.uwaterloo.mrafuse.context.framework.common.TypedServiceBinder
import java.util.*
import java.util.function.Consumer

class LockService : Service() {
    var isLocked = true
        private set(locked) {
            field = locked
            showLockNotification()
        }
    private val lockStateListeners: MutableList<LockStateListener> = LinkedList()
    private var notificationManager: NotificationManager? = null
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showLockNotification()
    }

    override fun onDestroy() {
        isLocked = true
        super.onDestroy()
    }

    @Synchronized
    fun lock() {
        isLocked = true
        Log.d(LOG_TAG, "Device locked")
        notifyLockStateListeners()
    }

    @Synchronized
    fun unlock() {
        isLocked = false
        Log.d(LOG_TAG, "Device unlocked")
        notifyLockStateListeners()
    }

    override fun onBind(intent: Intent): IBinder {
        return TypedServiceBinder.from(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun showLockNotification() {
        val icon = if (isLocked) R.drawable.ic_lock_black_24dp else R.drawable.ic_lock_open_black_24dp
        val title = if (isLocked) "LOCKED" else "UNLOCKED"
        val text = if (isLocked) "Device is locked" else "Device is unlocked"
        val id = 4711
        val notificationBuilder = Notification.Builder(this, "default")
                .setSmallIcon(icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
        notificationManager!!.notify(id, notificationBuilder.build())
    }

    private fun notifyLockStateListeners() {
        lockStateListeners.forEach(Consumer { eachListener: LockStateListener -> eachListener.onLockStateChanged(isLocked) })
    }

    fun addLockStateListener(lockStateListener: LockStateListener) {
        lockStateListeners.add(lockStateListener)
        notifyLockStateListeners()
    }

    fun removeLockStateListener(lockStateListener: LockStateListener?) {
        lockStateListeners.remove(lockStateListener)
    }

    interface LockStateListener {
        fun onLockStateChanged(lockState: Boolean)
    }

    companion object {
        private val LOG_TAG = LockService::class.java.simpleName
    }
}