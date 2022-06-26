package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var map: GoogleMap
    private var locationManager: LocationManager? = null
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 123

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        val reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        binding.reminderDataItem = reminderDataItem


        locationManager =  getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (isForegroundLocationPermissionApproved()){
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
        } else {
            requestForegroundLocationAccessPermissions()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

    }

    @TargetApi(29)
    private fun isForegroundLocationPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @TargetApi(29 )
    private fun requestForegroundLocationAccessPermissions() {
        if (isForegroundLocationPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
        {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED){
                Snackbar.make(
                    binding.reminderDescriptionView,
                    R.string.foreground_permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    .show()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.getLatitude(), location.getLongitude())
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        map.animateCamera(cameraUpdate)
        locationManager!!.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        val latitude = binding.reminderDataItem!!.latitude
        val longitude = binding.reminderDataItem!!.longitude
        val zoomLevel = 15f
        val homeLatLng = LatLng(latitude!!, longitude!!)
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        this.map.addMarker(MarkerOptions().position(homeLatLng))
        if (isForegroundLocationPermissionApproved()){
            this.map.setMyLocationEnabled(true)
        } else {
            requestForegroundLocationAccessPermissions()
        }
        this.map.uiSettings.setAllGesturesEnabled(false)
    }
}
