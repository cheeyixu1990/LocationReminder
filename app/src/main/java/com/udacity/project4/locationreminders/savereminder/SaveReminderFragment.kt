package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment.Companion.isRunningQAndAbove

const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private val REQUEST_BACKGROUND_PERMISSIONS_REQUEST_CODE = 456

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            if (isBackgroundLocationPermissionApproved()){
                val reminderData = reminderDataItem()
                addGeoFencingRequest(reminderData)
            } else {
                requestBackgroundLocationAccessPermissions()
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        if (_viewModel.marker.value != null) {
            mapFragment?.view?.visibility = View.VISIBLE
            mapFragment?.getMapAsync {
                map = it
                val location = LatLng(_viewModel.latitude.value!!, _viewModel.longitude.value!!)
                val zoomLevel = 15f
                map.addMarker(MarkerOptions()
                    .position(location)
                    .draggable(false))?.apply { showInfoWindow() }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
                map.uiSettings.setAllGesturesEnabled(false)
            }
        } else {
            mapFragment?.view?.visibility = View.GONE
        }
    }

    @TargetApi(29)
    private fun isBackgroundLocationPermissionApproved(): Boolean {
        return when {
            isRunningQAndAbove -> PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            else -> true
        }
    }

    @TargetApi(29)
    private fun requestBackgroundLocationAccessPermissions() {
        if (isBackgroundLocationPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val resultCode = REQUEST_BACKGROUND_PERMISSIONS_REQUEST_CODE

        if (isRunningQAndAbove) {
            requestPermissions(
                permissionsArray,
                resultCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BACKGROUND_PERMISSIONS_REQUEST_CODE)
        {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED){
                Snackbar.make(
                    binding.saveReminderView,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
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

    private fun reminderDataItem(): ReminderDataItem {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        val reminderData = ReminderDataItem(
            title = title,
            description = description,
            location = location,
            latitude = latitude,
            longitude = longitude
        )
        return reminderData
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFencingRequest(reminderData: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(reminderData.latitude!!, reminderData.longitude!!, 100f)
            .setExpirationDuration(6000000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_ENTER)
            .setLoiteringDelay(1000)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL or Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderData)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        getBroadcast(requireContext(),
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FLAG_MUTABLE or FLAG_UPDATE_CURRENT
            } else {
                FLAG_UPDATE_CURRENT
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
