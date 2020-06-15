package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    lateinit var mapFragment: SupportMapFragment

    private var addressLat: Double = 0.0
    private var addressLng: Double = 0.0


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

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        requireArguments().let {
            val locationArr = MapFragmentArgs.fromBundle(it).latLngLocation
            addressLat = locationArr[0].toDouble()
            addressLng = locationArr[1].toDouble()
            val myLocation = LatLng(addressLat, addressLng)
            mMap.addMarker(MarkerOptions().position(myLocation).title("موقعي"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))

        }
    }


}