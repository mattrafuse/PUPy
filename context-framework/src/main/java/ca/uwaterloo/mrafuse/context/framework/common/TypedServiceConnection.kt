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

import android.app.Service
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class TypedServiceConnection<T : Service?> : ServiceConnection {
    private var service: T? = null
    var isBound = false
        private set

    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as TypedServiceBinder<T>
        this.service = binder.service!!
        isBound = true
        onServiceConnected(this.service!!)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        isBound = false
        onServiceDisconnected(service)
    }

    fun get(): T? {
        return service
    }

    /**
     * Callback, overwrite if needed
     */
    fun onServiceConnected(service: T) {}

    /**
     * Callback, overwrite if needed
     */
    fun onServiceDisconnected(service: T?) {}
}