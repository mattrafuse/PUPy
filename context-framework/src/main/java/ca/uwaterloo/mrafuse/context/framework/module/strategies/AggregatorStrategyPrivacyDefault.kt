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
package ca.uwaterloo.mrafuse.context.framework.module.strategies

import android.util.Log
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.framework.module.AggregatorStrategy
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager
import kotlin.math.pow

class AggregatorStrategyPrivacyDefault : AggregatorStrategy {
    override fun aggregateData(alpha: Int): Double {
        Log.i(AggregatorStrategyPrivacyDefault::class.java.simpleName, "alpha: $alpha")
        var sum = 0.0
        var count = 0

        for (eachApi in PluginManager.instance!!.pluginListReadOnly) {
            if (PLUGIN_TYPE.privacy == eachApi.pluginType) {
                sum += eachApi.statusDataPrivacy!!.privacy
                count++
            }
        }
        val estimate = if ((sum / count).isNaN()) 0.0 else (sum / count)

        return if (estimate > 0) {
            val exponent = 1.0 - (1.0 / (estimate + .0001))
            1.0 - alpha.toDouble().pow(exponent) / alpha.toDouble()
        } else 1.0
    }
}