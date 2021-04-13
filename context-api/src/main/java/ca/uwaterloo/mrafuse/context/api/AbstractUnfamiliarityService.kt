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
package ca.uwaterloo.mrafuse.context.api

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity

/**
 * Base class for all risk plugin services with convenient methods.
 */
abstract class AbstractUnfamiliarityService : AbstractPluginService() {
    private val mBinder = UnfamiliarityPluginServiceBinder()
    protected fun publishUnfamiliarityUpdate(statusDataUnfamiliarity: StatusDataUnfamiliarity?) {
        val bundle = createUnfamiliarityDataBundle(statusDataUnfamiliarity)
        publishDataUpdate(bundle)
    }

    override val pluginType: PLUGIN_TYPE
        get() = PLUGIN_TYPE.unfamiliarity

    protected fun createUnfamiliarityDataBundle(statusDataUnfamiliarity: StatusDataUnfamiliarity?): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY, statusDataUnfamiliarity)
        bundle.putParcelable(PUPyConstants.KEY_COMPONENT_NAME, serviceComponentName)
        return bundle
    }

    override fun onNewPublishedData(msg: Message) {
        return
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class UnfamiliarityPluginServiceBinder : Binder() {
        // Return this instance of AbstractPluginService so clients can call public methods
        val service: AbstractUnfamiliarityService
            get() =// Return this instance of AbstractPluginService so clients can call public methods
                this@AbstractUnfamiliarityService
    }
}