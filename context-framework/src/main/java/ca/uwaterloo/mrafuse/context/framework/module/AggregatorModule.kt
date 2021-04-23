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
package ca.uwaterloo.mrafuse.context.framework.module

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import ca.uwaterloo.mrafuse.context.framework.common.PermissionHelper
import ca.uwaterloo.mrafuse.context.framework.common.Database
import java.lang.Integer.max

/**
 * Behavior is customizable through FusionStrategies.
 * Is triggered by the DecisionModule after plugin changes.
 * Starts the decision process of the DecisionModule after the plugin data has been fused.
 */
class AggregatorModule(private val context: Context, private val aggregatorStrategyPrivacy: AggregatorStrategy, private val aggregatorStrategyUnfamiliarity: AggregatorStrategy, private val aggregatorStrategyProximity: AggregatorStrategy) {
    private var locationManager: LocationManager? = null
    private val database = Database(context)

    private var lastAggregation = 0L
    fun aggregate(ruleModule: RuleModule) {

        //TODO: This is a hack. We should instead run aggregation at specific times, or something
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAggregation < 500) {
            return
        } else {
            lastAggregation = currentTime
        }

        val alpha = queryAlpha()
        val privacyAggregateResult = aggregatorStrategyPrivacy.aggregateData(alpha)
        val unfamiliarityAggregateResult = aggregatorStrategyUnfamiliarity.aggregateData(alpha)
        val proximityAggregateResult = aggregatorStrategyProximity.aggregateData(alpha)

        ruleModule.updateFunctionalityModules(privacyAggregateResult, unfamiliarityAggregateResult, proximityAggregateResult)
    }

    @SuppressLint("MissingPermission")
    fun queryAlpha(): Int {
        val perms = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (PermissionHelper.hasPermissionAny(context.applicationContext, perms)) {
            var loc: Location? = null
            if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                loc = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (loc != null) {
                val key = database.createKey(loc.latitude, loc.longitude)
                if (database.hasLocation(key)) {
                    val dbLoc = database.getLocation(key)
                    if ((System.currentTimeMillis() / 1000 / 60 / 60 / 6) - dbLoc.seen > 0) {
                        database.updateLocation(dbLoc, dbLoc.alpha + 1)
                        return max(dbLoc.alpha + 1, 3)
                    } else {
                        database.updateLocation(dbLoc)
                        return max(dbLoc.alpha, 3)
                    }
                } else {
                    database.addLocation(key, 3)
                    return 3
                }
            } else {
                Log.w(LOG_TAG, "No location available")
            }
        } else {
            Log.w(LOG_TAG, "No location permissions")
        }

        return 3
    }

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    companion object {
        private val LOG_TAG = AggregatorModule::class.java.simpleName
    }
}