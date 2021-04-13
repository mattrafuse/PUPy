package ca.uwaterloo.mrafuse.locationplugin

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ca.uwaterloo.mrafuse.locationplugin.common.Database
import ca.uwaterloo.mrafuse.locationplugin.common.PermissionHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_location_configuration.*
import java.io.IOException

// TODO: Vastly reduce the amount of work this activity is doing
class LocationConfigurationActivity : AppCompatActivity() {
    private var locationManager: LocationManager? = null
    private val database = Database.getInstance(this)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_configuration)

        Log.i(LOG_TAG, "onCreate")

        PermissionHelper.checkAndGetPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

        locationManager = getSystemService(LocationManager::class.java)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            it.isMyLocationEnabled = true
        }

        addSafeLocations()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        setSafeLocation.setOnClickListener {
            mapView.getMapAsync {
                database.addLocation(it.cameraPosition.target)
                addSafeLocations()
            }
        }

        removeLocations.setOnClickListener {
            database.removeLocations()
            mapView.getMapAsync { it.clear() }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        Log.i(LOG_TAG, "onResume")
        setLocationOnMap(tryGetLocation())
    }

    @SuppressLint("MissingPermission")
    private fun tryGetLocation(): Location? {
        try {
            if (PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(LOG_TAG, "have permissions")
                return locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun setLocationOnMap(location: Location?) {
        Log.i(LOG_TAG, "setLocationOnMap")
        mapView.getMapAsync { map ->

            var loc = location
            if (loc == null) {
                loc = tryGetLocation()
            }

            Log.i(LOG_TAG, loc.toString())

            if (loc != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), DEFAULT_ZOOM))
            } else {
                Log.i(LOG_TAG, "loc failed")
            }
        }
    }

    private fun addSafeLocations() {
        mapView.getMapAsync { map ->
            map.clear()
            database.getLocations().forEach {loc ->
                map.addCircle(CircleOptions().apply {
                    center(loc)
                    radius(50.0)
                    strokeColor(Color.argb(255, 0, 255, 0))
                    fillColor(Color.argb(64, 0, 255, 0))
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        val LOG_TAG = LocationConfigurationActivity::class.java.simpleName
        val DEFAULT_ZOOM = 15.0f
    }
}