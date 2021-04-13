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

import ca.uwaterloo.mrafuse.context.framework.R

/**
 * Created by fhdwsse
 */
object CommonUtils {
    fun getIconByScreenSize(screenSize: Double, blue: Boolean): Int {
        if (screenSize >= 7) {
            return if (blue) R.drawable.ic_computer_blue_24dp else R.drawable.ic_computer_black_24dp
        }
        return if (screenSize < 3) {
            if (blue) R.drawable.ic_watch_blue_24dp else R.drawable.ic_watch_black_24dp
        } else {
            if (blue) R.drawable.ic_phone_android_blue_24dp else R.drawable.ic_phone_android_black_24dp
        }
    }
}