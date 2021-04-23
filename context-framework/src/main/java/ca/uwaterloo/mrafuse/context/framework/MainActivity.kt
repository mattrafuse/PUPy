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
package ca.uwaterloo.mrafuse.context.framework

import android.Manifest
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ca.uwaterloo.mrafuse.context.api.PUPyConstants
import ca.uwaterloo.mrafuse.context.framework.common.PermissionHelper
import ca.uwaterloo.mrafuse.context.framework.common.TypedServiceConnection
import ca.uwaterloo.mrafuse.context.framework.lock.LockService
import ca.uwaterloo.mrafuse.context.framework.plugin.PluginManager
import ca.uwaterloo.mrafuse.context.framework.ui.PluginDetailFragment
import ca.uwaterloo.mrafuse.context.framework.ui.PluginDetailFragment.PluginDetailFragmentCallbacks
import ca.uwaterloo.mrafuse.context.framework.ui.PluginListFragment
import ca.uwaterloo.mrafuse.context.framework.ui.PluginListFragment.PluginListFragmentCallbacks

class MainActivity : AppCompatActivity(), PluginListFragmentCallbacks, PluginDetailFragmentCallbacks {
    private val lockService: TypedServiceConnection<LockService?> = TypedServiceConnection()
//    private var authService: AuthenticationFrameworkService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(LOG_TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionHelper.checkAndGetPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        bindService(Intent(this, LockService::class.java), lockService, Context.BIND_AUTO_CREATE)
        supportActionBar!!.title = getString(R.string.title_default)

        val authServiceIntent = Intent(this, AuthenticationFrameworkService::class.java)
        startForegroundService(authServiceIntent)

        showMainFragment()
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
//        authService?.onResume()
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fragment, tag)
                .commit()
    }

    private fun showMainFragment() {
        Log.i(LOG_TAG, "showMainFragment")
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.container, PluginListFragment())
                .commit()
    }

    private fun removeAllPlugins() {
        RemovePluginTask(packageManager) {removePackage(it)}.execute()
    }

    private fun removePackage(@Suppress("SameParameterValue") packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    override fun onPluginListItemSelected(position: Int) {
        val pluginClass: String = PluginManager.instance?.pluginListReadOnly?.get(position)!!.componentName!!.className
        switchFragment(PluginDetailFragment.newInstance(pluginClass), PluginDetailFragment::class.java.simpleName)
    }

    override fun onPluginRemoved() {
        fragmentManager.popBackStack()
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            fragmentManager.popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRemovePlugins -> {
                removeAllPlugins()
                true
            }
            R.id.menuRemoveFramework -> {
                removePackage("ca.uwaterloo.mrafuse.context")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class RemovePluginTask(val packageManager: PackageManager, val removePackage: (packageName: String) -> Unit) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void) {
            val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            for (eachInstalledPackage in installedPackages) {
                val packageName = eachInstalledPackage.packageName
                val metaData = eachInstalledPackage.applicationInfo.metaData ?: continue
                metaData.getString(PUPyConstants.META_STARTUP_SERVICE) ?: continue
                Log.d(LOG_TAG, "Removing $packageName")
                removePackage(packageName)
            }
            return
        }
    }

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
    }
}