package com.dawa.user.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.adapters.HomeMedicationsAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService
import com.dawa.user.network.data.Pageable
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.HomeActivity
import com.dawa.user.ui.UserActivity
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_home.*


class HomeFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var addressLat: Double = 0.0
    private var addressLng: Double = 0.0

    lateinit var progressDialog: MessageProgressDialog
    lateinit var medicationsAdapter: HomeMedicationsAdapter

    var hasMore = false
    var page = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        page = 1
        return inflater.inflate(R.layout.layout_home, container, false)
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {

        if (requestCode == LOCATION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    @SuppressLint("MissingPermission") // I handle it in another place !
    private fun updateLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 5000
        fusedLocationProviderClient = FusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.myLooper())
    }

    private fun getLocation() {
        if (addressLat != 0.0 && addressLng != 0.0) {
            getListOfMedications(page.toString(), null)
            return
        }

        if (checkLocationPermission()) {
            updateLocation()
        } else {
            askForLocationPermission()
        }
    }

    var firstTime = true

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            addressLat = locationResult.lastLocation.latitude
            addressLng = locationResult.lastLocation.longitude
            if (firstTime) {
                firstTime = false
                getListOfMedications(page.toString(), null)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())
        getLocation()

        if (PreferenceManagerService.retrieveToken().isEmpty()) {
            signIn.visibility = View.VISIBLE
            signIn.setOnClickListener {
                val intent = Intent(requireContext(), UserActivity::class.java)
                requireActivity().startActivity(intent)
            }
        } else {
            signIn.visibility = View.GONE
        }

        medicationsAdapter = HomeMedicationsAdapter()
        val gridLayout = GridLayoutManager(requireActivity(), 2)
        medications_list.layoutManager = gridLayout
        medications_list.adapter = medicationsAdapter
        val query = searchView.query!!.toString()

        medications_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (gridLayout.findLastVisibleItemPosition() == gridLayout.itemCount - 1 && hasMore) {
                    page++
                    if (query.isEmpty()) {
                        getListOfMedications(page.toString(), null)
                    } else {
                        getListOfMedications("", query)
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isEmpty()) {
                    getListOfMedications(page.toString(), null)
                } else {
                    getListOfMedications("", query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        my_medications.setOnClickListener {
            if (PreferenceManagerService.retrieveToken().isEmpty()) {
                Toast.makeText(requireContext(), "من فضلك قم بتسجيل الدخول اولا", Toast.LENGTH_LONG)
                    .show();
            } else {
                val toMyMedications = HomeFragmentDirections.toMyMedications()
                Navigation.findNavController(it).navigate(toMyMedications)
            }
        }

    }


    private fun getListOfMedications(pageNumber: String?, query: String?) {
        RetrofitClient.INSTANCE.listMedications(query,
                addressLat.toString(),
                addressLng.toString(),
                "20",
                pageNumber.toString()).onErrorReturn {
            Pageable()
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                if (it.pagination == null) {
                    progressDialog.generalError()
                } else {
                    page = it.pagination!!.currentPage
                    hasMore = it.next
                    medicationsAdapter.add(it.pageAbleList, query)
                }
                AppExecutorsService.handlerDelayed({
                    progressDialog.dismiss()
                }, 1000)
            }
        }
    }

}