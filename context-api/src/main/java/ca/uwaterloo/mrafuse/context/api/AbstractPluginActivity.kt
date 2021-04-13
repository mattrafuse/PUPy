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

import android.app.Activity
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity

/**
 * Base class for plugin activities with convenient methods for sending data to the framework.
 */
open class AbstractPluginActivity : Activity() {
    protected fun publishPrivacyData(statusDataPrivacy: StatusDataPrivacy?) {
        val intent = Intent(PUPyConstants.ACTION_LOCAL_SEND_PRIVACY)
        intent.putExtra(PUPyConstants.KEY_STATUS_DATA_PRIVACY, statusDataPrivacy)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    protected fun publishUnfamiliarityData(statusDataUnfamiliarity: StatusDataUnfamiliarity?) {
        val intent = Intent(PUPyConstants.ACTION_LOCAL_SEND_UNFAMILIARITY)
        intent.putExtra(PUPyConstants.KEY_STATUS_DATA_UNFAMILIARITY, statusDataUnfamiliarity)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    protected fun publishProximityData(statusDataProximity: StatusDataProximity?) {
        val intent = Intent(PUPyConstants.ACTION_LOCAL_SEND_PROXIMITY)
        intent.putExtra(PUPyConstants.KEY_STATUS_DATA_PROXIMITY, statusDataProximity)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}