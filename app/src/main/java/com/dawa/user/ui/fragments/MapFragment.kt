package com.dawa.user.ui.fragments

import android.location.Geocoder
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.Serializable
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    lateinit var mapFragment: SupportMapFragment

    private var addressLat: Double = 0.0
    private var addressLng: Double = 0.0

    lateinit var locationMarker: Marker


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPosition.setOnClickListener {
            val latLngAddress = LatLng(this.addressLat, this.addressLng)
            val address = getLocationAddressString(latLngAddress)
            val locationData =
                LocationData(latLngAddress.latitude, latLngAddress.longitude, address.toString())
            val toAddMed = MapFragmentDirections.toAddMed(locationData)
            Navigation.findNavController(setPosition).navigate(toAddMed)
        }
    }

    private fun getLocationAddressString(location: LatLng): String? {
        val myLocation = Geocoder(requireContext(), Locale.getDefault())
        val myList = myLocation.getFromLocation(location.latitude, location.longitude, 1)
        val address = myList[0]
        var addressStr: String? = ""
        addressStr += address.getAddressLine(0) + ", "
        addressStr += address.getAddressLine(1) + ", "
        addressStr += address.getAddressLine(2)
        return addressStr;
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        requireArguments().let {
            val locationArr = MapFragmentArgs.fromBundle(it).latLngLocation
            addressLat = locationArr[0].toDouble()
            addressLng = locationArr[1].toDouble()
            val myLocation = LatLng(addressLat, addressLng)
            this.locationMarker =
                mMap.addMarker(MarkerOptions().position(myLocation).title("موقعي"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
        }

        mMap.setOnMapLongClickListener {
            addressLat = it.latitude
            addressLng = it.longitude
            mMap.clear()
            this.locationMarker = mMap.addMarker(MarkerOptions().position(it).title("موقعي"))
            AppExecutorsService.handlerDelayed({
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }, 1000)
        }

    }


}

data class LocationData(val latDouble: Double, val lngDouble: Double, val address: String) :
    Serializable