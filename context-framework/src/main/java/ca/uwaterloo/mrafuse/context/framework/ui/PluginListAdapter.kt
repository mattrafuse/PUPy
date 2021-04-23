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
package ca.uwaterloo.mrafuse.context.framework.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.framework.R
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginInfo
import java.text.SimpleDateFormat
import java.util.*

class PluginListAdapter(private val ctxt: Context, private val plugins: List<PluginInfo>) : ArrayAdapter<PluginInfo?>(ctxt, R.layout.listitem_plugins, plugins) {
    @SuppressLint("SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.listitem_plugins, parent, false)
        val rowTitle = rowView.findViewById<View>(R.id.rowTitle) as TextView
        val rowDesc = rowView.findViewById<View>(R.id.rowDescription) as TextView
        val rowValue = rowView.findViewById<View>(R.id.rowValue) as TextView
        val rowLastUpdated = rowView.findViewById<View>(R.id.rowLastUpdated) as TextView
        val rowState = rowView.findViewById<View>(R.id.rowState) as TextView
        val imageView = rowView.findViewById<View>(R.id.icon) as ImageView
        val pluginInfo = plugins[position]
        rowTitle.text = pluginInfo.title
        rowDesc.text = pluginInfo.description
        if (PLUGIN_TYPE.privacy == pluginInfo.pluginType) {
            rowValue.text = "Privacy: " + pluginInfo.statusDataPrivacy!!.privacy
            rowState.text = "State: " + pluginInfo.statusDataPrivacy!!.status
        } else if (PLUGIN_TYPE.unfamiliarity == pluginInfo.pluginType) {
            val info = pluginInfo.statusDataUnfamiliarity!!.info
            if (info != null) rowDesc.text = info //TODO Why override desc?
            rowValue.text = "Unfamiliarity: " + pluginInfo.statusDataUnfamiliarity!!.unfamiliarity
            rowState.text = "State: " + pluginInfo.statusDataUnfamiliarity!!.status
        } else if (PLUGIN_TYPE.proximity == pluginInfo.pluginType) {
            val info = pluginInfo.statusDataProximity!!.info
            if (info != null) rowDesc.text = info //TODO Why override desc?
            rowValue.text = "Proximity: " + pluginInfo.statusDataProximity!!.proximity
            rowState.text = "State: " + pluginInfo.statusDataProximity!!.status
        }
        rowLastUpdated.text = "Last updated: " +
                if (pluginInfo.lastUpdate != null) SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.CANADA).format(pluginInfo.lastUpdate!!.time) else "UNKNOWN"
        imageView.setImageDrawable(pluginInfo.icon)
        return rowView
    }

}