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
package ca.uwaterloo.mrafuse.context.framework.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import ca.uwaterloo.mrafuse.context.api.PUPyConstants.PLUGIN_TYPE
import ca.uwaterloo.mrafuse.context.framework.R
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginInfo
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager.PluginChangeListener
import java.text.SimpleDateFormat
import java.util.*

class PluginDetailFragment : Fragment(), View.OnClickListener, PluginChangeListener {
    private var callbacks: PluginDetailFragmentCallbacks? = null
    private var currentApi: PluginInfo? = null
    private var txtDescription: TextView? = null
    private var txtComponentName: TextView? = null
    private var txtAuthValue: TextView? = null
    private var txtStatus: TextView? = null
    private var txtType: TextView? = null
    private var txtAuthValueLabel: TextView? = null
    private var buttonRequest: Button? = null
    private var buttonConfig: Button? = null
    private var txtLastUpdate: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_plugin_detail, container, false)
        txtDescription = rootView.findViewById<View>(R.id.plugin_detail_description) as TextView
        txtComponentName = rootView.findViewById<View>(R.id.plugin_detail_component_name) as TextView
        txtAuthValue = rootView.findViewById<View>(R.id.plugin_detail_auth_value) as TextView
        txtLastUpdate = rootView.findViewById<View>(R.id.plugin_detail_last_update) as TextView
        txtStatus = rootView.findViewById<View>(R.id.plugin_detail_status) as TextView
        txtType = rootView.findViewById<View>(R.id.plugin_detail_type) as TextView
        txtAuthValueLabel = rootView.findViewById<View>(R.id.plugin_detail_auth_value_label) as TextView
        buttonRequest = rootView.findViewById<View>(R.id.plugin_detail_request_authentication) as Button
        buttonRequest!!.setOnClickListener(this)
        buttonConfig = rootView.findViewById<View>(R.id.plugin_detail_show_configuration) as Button
        buttonConfig!!.setOnClickListener(this)
        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun setDetails() {
        currentApi = PluginManager.instance!!.getPluginInfo(arguments!!.getString(ARG_PLUGIN_PACKAGE)!!)
        if (currentApi == null) {
            Toast.makeText(activity, "Plugin " + activity!!.title + " is no longer available", Toast.LENGTH_LONG).show()
            callbacks!!.onPluginRemoved()
            return
        }

        activity!!.title = currentApi!!.title
        if (currentApi!!.explicitAuthComponentName == null) buttonRequest!!.visibility = View.GONE
        if (currentApi!!.configurationComponentName == null) buttonConfig!!.visibility = View.GONE
        txtDescription!!.text = currentApi!!.description
        txtComponentName!!.text = currentApi!!.componentName!!.className
        if (currentApi!!.lastUpdate != null) {
            txtLastUpdate!!.text = SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.CANADA).format(currentApi!!.lastUpdate!!.time)
        }
        if (PLUGIN_TYPE.privacy == currentApi!!.pluginType) {
            txtAuthValueLabel!!.text = "Privacy"
            txtAuthValue!!.text = currentApi!!.statusDataPrivacy!!.privacy.toString()
            txtStatus!!.text = currentApi!!.statusDataPrivacy!!.status.toString()
            txtType!!.text = PLUGIN_TYPE.privacy.toString().toUpperCase()
        } else if (PLUGIN_TYPE.unfamiliarity == currentApi!!.pluginType) {
            txtAuthValueLabel!!.text = "Unfamiliarity"
            txtAuthValue!!.text = currentApi!!.statusDataUnfamiliarity!!.unfamiliarity.toString()
            txtStatus!!.text = currentApi!!.statusDataUnfamiliarity!!.status.toString()
            txtType!!.text = PLUGIN_TYPE.unfamiliarity.toString().toUpperCase()
        } else if (PLUGIN_TYPE.proximity == currentApi!!.pluginType) {
            txtAuthValueLabel!!.text = "Proximity"
            txtAuthValue!!.text = currentApi!!.statusDataProximity!!.proximity.toString()
            txtStatus!!.text = currentApi!!.statusDataProximity!!.status.toString()
            txtType!!.text = PLUGIN_TYPE.unfamiliarity.toString().toUpperCase()
        } else if (PLUGIN_TYPE.functionality == currentApi!!.pluginType) {
            txtAuthValueLabel!!.visibility = View.GONE
            txtAuthValue!!.visibility = View.GONE
            txtStatus!!.visibility = View.GONE
            txtLastUpdate!!.visibility = View.GONE
            txtType!!.text = PLUGIN_TYPE.functionality.toString().toUpperCase()
        }
    }

    override fun onClick(v: View) {
        val intent = Intent()
        if (v.id == R.id.plugin_detail_request_authentication) {
            intent.component = currentApi!!.explicitAuthComponentName
        } else if (v.id == R.id.plugin_detail_show_configuration) {
            intent.component = currentApi!!.configurationComponentName
        }
        startActivity(intent)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        callbacks = try {
            activity as PluginDetailFragmentCallbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement PluginListFragmentCallbacks.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onResume() {
        super.onResume()
        setDetails()
        PluginManager.instance!!.addPluginChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PluginManager.instance!!.removePluginChangeListener(this)
    }

    override fun onPluginsChanged() {
        setDetails()
    }

    interface PluginDetailFragmentCallbacks {
        fun onPluginRemoved()
    }

    companion object {
        private const val ARG_PLUGIN_PACKAGE = "arg_plugin_package"
        fun newInstance(pluginPackage: String?): PluginDetailFragment {
            val args = Bundle()
            args.putString(ARG_PLUGIN_PACKAGE, pluginPackage)
            val fragment = PluginDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}