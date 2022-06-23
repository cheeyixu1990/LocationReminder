package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), LocationListener {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var locationManager: LocationManager? = null
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            if (_viewModel.marker.value == null){
                Snackbar.make(requireContext(), map_selection_view, "Please long press to select a location or click on a Point of Interest.", Snackbar.LENGTH_SHORT).show()
            } else {
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        locationManager =  requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
        mapFragment?.getMapAsync{
            map = it
            val location: LatLng
            val zoomLevel: Float
            val singapore = LatLng(1.3521, 103.8198)
            if (_viewModel.marker.value != null){
                location = LatLng(_viewModel.latitude.value!!, _viewModel.longitude.value!!)
                zoomLevel = 13f
                addMarker(location, map)
            } else {
                location = singapore
                zoomLevel = 10f
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
            map.setMyLocationEnabled(true)
            setMapLongClick(map)
            setPoiSelected(map)
        }
    }

    private fun setPoiSelected(map: GoogleMap) {
        map.setOnPoiClickListener {
            val latLng = it.latLng
            _viewModel.reminderSelectedLocationStr.value = it.name
            addMarker(latLng, map)
            setLatLng(latLng)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            _viewModel.reminderSelectedLocationStr.value = "Custom Location"
            addMarker(it, map)
            setLatLng(it)

        }
    }

    private fun setLatLng(it: LatLng) {
        _viewModel.latitude.value = it.latitude
        _viewModel.longitude.value = it.longitude
    }

    fun addMarker(
        latLng: LatLng,
        map: GoogleMap
    ) {
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            latLng.latitude,
            latLng.longitude
        )

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(getString(R.string.dropped_pin))
            .snippet(snippet)
            .draggable(true)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        if (_viewModel.marker.value == null) {
            _viewModel.marker.value = map.addMarker(
                markerOptions
            )
        } else {
            _viewModel.marker.value!!.remove()
            _viewModel.marker.value = map.addMarker(
                markerOptions
            )
        }
    }

    private fun onLocationSelected() {

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onLocationChanged(it: Location) {
        if (_viewModel.marker.value == null){
            val latLng = LatLng(it.getLatitude(), it.getLongitude())
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
            map.animateCamera(cameraUpdate)
        }
        locationManager!!.removeUpdates(this)
    }


}
