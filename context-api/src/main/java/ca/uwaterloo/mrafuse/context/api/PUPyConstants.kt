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

object PUPyConstants {
    const val MSG_ADD_PLUGIN = 1
    const val MSG_PRIVACY = 2
    const val MSG_UNFAMILIARITY = 3
    const val MSG_PROXIMITY = 4
    const val MSG_POLL_DATA = 5
    const val MSG_PUBLISH_DATA = 6
    const val ACTION_LOCAL_SEND_PRIVACY = "actionLocalSendPrivacy"
    const val ACTION_LOCAL_SEND_UNFAMILIARITY = "actionLocalSendUnfamiliarity"
    const val ACTION_LOCAL_SEND_PROXIMITY = "actionLocalSendProximity"
    const val ACTION_LOCAL_SEND_UPDATE = "actionLocalSendUpdate"
    const val KEY_COMPONENT_NAME = "keyComponentName"
    const val KEY_STATUS_DATA_PRIVACY = "keyStatusDataPrivacy"
    const val KEY_STATUS_DATA_UNFAMILIARITY = "keyStatusDataUnfamiliarity"
    const val KEY_STATUS_DATA_PROXIMITY = "keyStatusDataProximity"
    const val PUPY_PACKAGE = "ca.uwaterloo.mrafuse.context.framework"
    const val PUPY_SERVICE = ".AuthenticationFrameworkService"
    const val META_API_VERSION = "apiVersion"
    const val META_PLUGIN_TYPE = "pluginType"
    const val META_TITLE = "title"
    const val META_DESCRIPTION = "description"
    const val META_CONFIGURATION = "configurationActivity"
    const val META_EXPLICIT_AUTH = "explicitAuthActivity"
    const val META_STARTUP_SERVICE = "startupService"
    const val META_IMPLICIT = "implicit"

    enum class PLUGIN_TYPE {
        privacy, unfamiliarity, proximity, functionality
    }
}