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
package ca.uwaterloo.mrafuse.context.framework.module

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import ca.uwaterloo.mrafuse.context.framework.AdminReceiver
import ca.uwaterloo.mrafuse.context.framework.common.TypedServiceConnection
import ca.uwaterloo.mrafuse.context.framework.lock.LockService
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager.PluginChangeListener
import java.util.*

/**
 * Behavior is customizable by using different DecisionStrategy implementations.
 * Polls data from plugins based on a given pollingInterval from the used DecisionStrategy.
 * If a plugin has changed it will trigger the FusionModule.
 * The decision making process is started from the FusionModule after the data has been fused.
 */
class RuleModule(private val context: Context, privacyAggregatorStrategy: AggregatorStrategy,
                 unfamiliarityAggregatorStrategy: AggregatorStrategy, proximityAggregatorStrategy: AggregatorStrategy) : PluginChangeListener {
    private val aggregatorModule: AggregatorModule
    private val lockServiceConnection: TypedServiceConnection<LockService?> = TypedServiceConnection()
    private val timer: Timer
    private val pollingInterval: Int //ms

    fun start() {
        context.bindService(Intent(context, LockService::class.java), lockServiceConnection, Context.BIND_AUTO_CREATE)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                PluginManager.instance!!.pollDataFromPlugins()
            }
        }, 0, pollingInterval.toLong())
    }

    fun stop() {
        timer.cancel()
        Log.d(LOG_TAG, "Stopped")
    }

    fun updateFunctionalityModules(privacyLevel: Double, unfamiliarityLevel: Double, proximityLevel: Double) {
        Log.i(LOG_TAG, "privacyLevel: $privacyLevel")
        Log.i(LOG_TAG, "unfamiliarityLevel: $unfamiliarityLevel")
        Log.i(LOG_TAG, "proximityLevel: $proximityLevel")

        PluginManager.instance!!.postDataToPlugins(privacyLevel, unfamiliarityLevel, proximityLevel)
    }

    override fun onPluginsChanged() {
        aggregatorModule.aggregate(this@RuleModule)
    }

    companion object {
        val LOG_TAG = RuleModule::class.java.simpleName
    }

    init {
        aggregatorModule = AggregatorModule(context, privacyAggregatorStrategy, unfamiliarityAggregatorStrategy, proximityAggregatorStrategy)
        timer = Timer()
        pollingInterval = 5000
    }
}