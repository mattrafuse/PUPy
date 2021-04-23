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
package ca.uwaterloo.mrafuse.context.api

import android.content.ComponentName
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.util.Log
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity

/**
 * Base class for all confidence plugin services with convenient methods.
 */
abstract class AbstractFunctionalityService : AbstractPluginService() {
    private val mBinder = FunctionalityPluginServiceBinder()

    override val pluginType: PLUGIN_TYPE
        get() = PLUGIN_TYPE.privacy

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onDataUpdateRequest() {
        return
    }

    inner class FunctionalityPluginServiceBinder : Binder() {
        // Return this instance of AbstractPluginService so clients can call public methods
        val service: AbstractFunctionalityService
            get() =// Return this instance of AbstractPluginService so clients can call public methods
                this@AbstractFunctionalityService
    }
}