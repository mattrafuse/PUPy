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
package ca.uwaterloo.mrafuse.context.framework.plugin

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.Messenger
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.api.model.StatusDataPrivacy
import ca.uwaterloo.mrafuse.context.api.model.StatusDataProximity
import ca.uwaterloo.mrafuse.context.api.model.StatusDataUnfamiliarity
import java.util.*

/**
 * Holds all pieces of information that a plugin can deliver.
 */
class PluginInfo(var messenger: Messenger?) : Comparable<PluginInfo> {
    var title: String? = null
    var description: String? = null
    var apiVersion = 0
    var isImplicit = false
    var lastUpdate: Calendar? = null
    var icon: Drawable? = null
    var componentName: ComponentName? = null
    var configurationComponentName: ComponentName? = null
    var explicitAuthComponentName: ComponentName? = null
    var statusDataPrivacy: StatusDataPrivacy? = null
    var statusDataUnfamiliarity: StatusDataUnfamiliarity? = null
    var statusDataProximity: StatusDataProximity? = null
    var pluginType: PLUGIN_TYPE? = null

    override fun compareTo(other: PluginInfo): Int {
        return componentName!!.compareTo(other.componentName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as PluginInfo
        if (apiVersion != that.apiVersion) return false
        if (isImplicit != that.isImplicit) return false
        if (if (title != null) title != that.title else that.title != null) return false
        if (if (description != null) description != that.description else that.description != null) return false
        if (if (lastUpdate != null) lastUpdate != that.lastUpdate else that.lastUpdate != null) return false
        if (if (icon != null) icon != that.icon else that.icon != null) return false
        if (if (messenger != null) messenger != that.messenger else that.messenger != null) return false
        if (if (componentName != null) componentName != that.componentName else that.componentName != null) return false
        if (if (configurationComponentName != null) configurationComponentName != that.configurationComponentName else that.configurationComponentName != null) return false
        if (if (explicitAuthComponentName != null) explicitAuthComponentName != that.explicitAuthComponentName else that.explicitAuthComponentName != null) return false
        if (if (statusDataPrivacy != null) statusDataPrivacy != that.statusDataPrivacy else that.statusDataPrivacy != null) return false
        return if (if (statusDataUnfamiliarity != null) statusDataUnfamiliarity != that.statusDataUnfamiliarity else that.statusDataUnfamiliarity != null) false else pluginType === that.pluginType
    }

    override fun hashCode(): Int {
        var result = if (title != null) title.hashCode() else 0
        result = 31 * result + if (description != null) description.hashCode() else 0
        result = 31 * result + apiVersion
        result = 31 * result + if (isImplicit) 1 else 0
        result = 31 * result + if (lastUpdate != null) lastUpdate.hashCode() else 0
        result = 31 * result + if (icon != null) icon.hashCode() else 0
        result = 31 * result + if (messenger != null) messenger.hashCode() else 0
        result = 31 * result + if (componentName != null) componentName.hashCode() else 0
        result = 31 * result + if (configurationComponentName != null) configurationComponentName.hashCode() else 0
        result = 31 * result + if (explicitAuthComponentName != null) explicitAuthComponentName.hashCode() else 0
        result = 31 * result + if (statusDataPrivacy != null) statusDataPrivacy.hashCode() else 0
        result = 31 * result + if (statusDataUnfamiliarity != null) statusDataUnfamiliarity.hashCode() else 0
        result = 31 * result + if (pluginType != null) pluginType.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "PluginInfo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", apiVersion=" + apiVersion +
                ", implicit=" + isImplicit +
                ", lastUpdate=" + lastUpdate +
                ", icon=" + icon +
                ", messenger=" + messenger +
                ", componentName=" + componentName +
                ", configurationComponentName=" + configurationComponentName +
                ", explicitAuthComponentName=" + explicitAuthComponentName +
                ", statusDataPrivacy=" + statusDataPrivacy +
                ", statusDataUnfamiliarity=" + statusDataUnfamiliarity +
                ", pluginType=" + pluginType +
                '}'
    }

}