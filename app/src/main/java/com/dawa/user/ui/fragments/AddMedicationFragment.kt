package com.dawa.user.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.handlers.*
import com.dawa.user.network.data.MedicationCreationForm
import com.dawa.user.network.data.MedicineCategoryDTO
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_add_medication.*
import okhttp3.MultipartBody
import java.util.*


const val LOCATION_REQUEST = 300

class AddMedicationFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private lateinit var progressDialog: MessageProgressDialog
    private lateinit var categoryList: List<MedicineCategoryDTO>

    lateinit var takePhotoService: TakePhotoService
    lateinit var imageUri: Uri
    private var multiPartFile: MultipartBody.Part? = null

    private var selectedCategoryId = 0


    private lateinit var addressTitle: String
    private var addressLat: Double = 0.0
    private var addressLng: Double = 0.0


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_add_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        takePhotoService = TakePhotoService(this, requireContext())
        loadCategories()

        add_image.setOnClickListener {
            showPictureDialog()
        }


        add_med.setOnClickListener {
            progressDialog.loading()
            postMedicationAction()
        }

        location_from_map.setOnClickListener {
            val openMap = AddMedicationFragmentDirections.toMap(floatArrayOf(addressLat.toFloat(),
                    addressLng.toFloat()))
            Navigation.findNavController(it).navigate(openMap)
        }

        requireArguments().let {
            val locationData = AddMedicationFragmentArgs.fromBundle(it).locationData
            if (locationData != null) {
                val location = Location("Test")
                location.latitude = locationData.latDouble
                location.longitude = locationData.lngDouble
                updateAddressUI(location)
            } else {
                getLocation()
            }
        }


    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> {
                    takePhotoService.openGallery()
                }
                1 -> {
                    imageUri = takePhotoService.capturePhoto()
                }
            }
        }
        pictureDialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPERATION_CAPTURE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                multiPartFile = takePhotoService.resultForImageCapture(imageUri, medication_image)
            } else {
                Toast.makeText(requireContext(),
                        "Something went wrong take chose photo again",
                        Toast.LENGTH_LONG).show()
            }
            OPERATION_CHOOSE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                multiPartFile = takePhotoService.resultForImageGallery(data, medication_image)
            } else {
                Toast.makeText(requireContext(),
                        "Something went wrong please chose photo again",
                        Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun getLocation() {
        if (checkLocationPermission()) {
            updateLocation()
        } else {
            askForLocationPermission()
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

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            updateAddressUI(location)
        }
    }

    private fun updateAddressUI(location: Location) {
        if (context != null && isAdded) {
            val geocoder = Geocoder(requireContext(), Locale("ar"))
            val currentAddress =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)[0]
            addressTitle = currentAddress.getAddressLine(0)
            address.text = addressTitle
            addressLat = location.latitude
            addressLng = location.longitude
        }
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


    private fun loadCategories() {
        RetrofitClient.INSTANCE.listMedicationsCategories().onErrorReturn { mutableListOf() }
            .doOnRequest {
                AppExecutorsService.mainThread().execute {
                    progressDialog.loading()
                }
            }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                    if (it.isNotEmpty()) {
                        categoryList = it
                        val categories = mutableListOf<String>()
                        it.forEach { cat -> categories.add(cat.name) }
                        val adapter = ArrayAdapter(requireContext(),
                                R.layout.support_simple_spinner_dropdown_item,
                                categories)
                        category_spinner.adapter = adapter
                        adapter.notifyDataSetChanged()
                        category_spinner.onItemSelectedListener = this
                    }

                }
            }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("AddMed", "leave this empty")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategoryId = categoryList[position].id
        Log.d("AddMed", "selected is $selectedCategoryId")
    }

    private fun isNotValidData(): Boolean {
        return (medicationName.text.isBlank() || address.text.isBlank() || selectedCategoryId == 0 || multiPartFile == null)
    }


    private fun postMedicationAction() {

        if (isNotValidData()) {
            Toast.makeText(requireContext(), "Please Fill Missing Fields", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
            return
        }

        val userData = PreferenceManagerService.retrieveUserData()
        val medicationCreationForm = MedicationCreationForm(medicationName.text.toString(),
                addressTitle,
                addressLat,
                addressLng,
                selectedCategoryId,
                userData.id)

        RetrofitClient.INSTANCE.addMedication(multiPartFile, medicationCreationForm).onErrorReturn {
            it.printStackTrace()
            ResponseWrapper(false, getString(R.string.general_error), null)
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                progressDialog.show(it.message)
                if (it.success) {
                    progressDialog.dismiss()
                    AppExecutorsService.handlerDelayed({
                        requireActivity().supportFragmentManager.popBackStack()
                    }, 1000)
                } else {
                    progressDialog.generalError()
                    AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 1000)
                }

            }

        }
    }


}