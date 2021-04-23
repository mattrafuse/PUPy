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

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.fragment.app.Fragment
import ca.uwaterloo.mrafuse.context.framework.R
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager.PluginChangeListener

class PluginListFragment : Fragment(), PluginChangeListener {
    private var listView: ListView? = null
    private var callbacks: PluginListFragmentCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_plugin_list, container, false)
        listView = rootView.findViewById<View>(R.id.listPlugins) as ListView
        initList(rootView)
        activity!!.title = getString(R.string.app_name)
        PluginManager.instance!!.addPluginChangeListener(this)
        return rootView
    }

    private fun initList(rootView: View) {
        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id -> callbacks!!.onPluginListItemSelected(position) }
        Log.i(LOG_TAG, PluginManager.instance!!.pluginListReadOnly.size.toString())
        listView!!.adapter = PluginListAdapter(rootView.context, PluginManager.instance!!.pluginListReadOnly)
    }

    interface PluginListFragmentCallbacks {
        fun onPluginListItemSelected(position: Int)
    }

    override fun onPluginsChanged() {
        listView!!.invalidateViews()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        PluginManager.instance!!.addPluginChangeListener(this)
        callbacks = try {
            activity as PluginListFragmentCallbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement PluginListFragmentCallbacks.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        PluginManager.instance!!.removePluginChangeListener(this)
        callbacks = null
    }

    companion object {
        val LOG_TAG = PluginListFragment::class.java.toString()
    }
}