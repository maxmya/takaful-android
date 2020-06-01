package com.dawa.user.ui.fragments

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.dawa.user.handlers.*
import com.dawa.user.network.data.MedicationCreationForm
import com.dawa.user.network.data.MedicineCategoryDTO
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_add_medication.*
import kotlinx.android.synthetic.main.layout_change_profile.*
import okhttp3.MultipartBody
import java.io.File


class AddMedicationFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var progressDialog: MessageProgressDialog
    private lateinit var categoryList: List<MedicineCategoryDTO>

    lateinit var takePhotoService: TakePhotoService
    lateinit var imageUri: Uri
    private var multiPartFile: MultipartBody.Part? = null

    private var selectedCategoryId = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        val pictureDialogItems = arrayOf(
            "Select photo from gallery",
            "Capture photo from camera"
        )
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
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    multiPartFile =
                        takePhotoService.resultForImageCapture(imageUri, medication_image)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong take chose photo again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    multiPartFile = takePhotoService.resultForImageGallery(data, medication_image)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong please chose photo again",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun loadCategories() {
        RetrofitClient
            .INSTANCE
            .listMedicationsCategories()
            .onErrorReturn { mutableListOf() }
            .doOnRequest {
                AppExecutorsService.mainThread().execute {
                    progressDialog.loading()
                }
            }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                    if (it.isNotEmpty()) {
                        categoryList = it
                        val categories = mutableListOf<String>()
                        it.forEach { cat -> categories.add(cat.name) }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.support_simple_spinner_dropdown_item,
                            categories
                        )
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
        return (medicationName.text.isBlank() || address.text.isBlank() || selectedCategoryId == 0)
    }


    private fun postMedicationAction() {

        if (isNotValidData()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.fill_field_please),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val userData = PreferenceManagerService.retrieveUserData()
        val medicationCreationForm =
            MedicationCreationForm(
                medicationName.text.toString(),
                address.text.toString(),
                0.0,
                0.0,
                selectedCategoryId,
                userData.id
            )

        RetrofitClient
            .INSTANCE
            .addMedication(multiPartFile, medicationCreationForm)
            .onErrorReturn {
                it.printStackTrace()
                ResponseWrapper(false, getString(R.string.general_error), null)
            }
            .doOnRequest {
                AppExecutorsService.mainThread().execute {
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.show(it.message)
                    if (it.success) {
                        progressDialog.dismiss()
                    } else {
                        progressDialog.generalError()
                    }
                }

            }
    }

}