package com.dawa.user.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService
import com.dawa.user.handlers.OPERATION_CAPTURE_PHOTO
import com.dawa.user.handlers.OPERATION_CHOOSE_PHOTO
import com.dawa.user.handlers.TakePhotoService
import com.dawa.user.network.data.*
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.utils.StringUtils
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_change_profile.*
import kotlinx.android.synthetic.main.layout_change_profile.fieldFullName
import kotlinx.android.synthetic.main.layout_change_profile.fieldPhone
import okhttp3.MultipartBody
import java.util.regex.Pattern


class ProfileFragment : Fragment() {


    lateinit var progressDialog: MessageProgressDialog
    lateinit var takePhotoService: TakePhotoService
    lateinit var imageUri: Uri
    private var multiPartFile: MultipartBody.Part? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_change_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        takePhotoService = TakePhotoService(
            this,
            requireContext()
        )

        loadData()
        StringUtils.maskPhoneField(fieldPhone)
        uploadImage.setOnClickListener { showPictureDialog() }
        editUserProfileAction()
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


    private fun editUserProfileAction() {

        btnLogin.setOnClickListener {

            if (!isValidFields())
                return@setOnClickListener

            val userData = PreferenceManagerService.retrieveUserData();
            val profileRequest = ChangeProfileRequest(
                fieldFullName.text.toString().trim(),
                userData.phone,
                StringUtils.getUnmaskedPhone(fieldPhone)
            )

            makeRegisterWithNetworkCall(profileRequest, multiPartFile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    multiPartFile =
                        takePhotoService.resultForImageCapture(imageUri, profile_picture)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong take chose photo again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    multiPartFile = takePhotoService.resultForImageGallery(data, profile_picture)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong please chose photo again",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }


    private fun makeRegisterWithNetworkCall(
        changeProfileBody: ChangeProfileRequest,
        image: MultipartBody.Part?
    ) {
        if (image == null) Log.d("TAG", "test")
        RetrofitClient
            .INSTANCE
            .changeUserProfile(changeProfileBody, image)
            .onErrorReturn {
                it.printStackTrace()
                ErrorClass(false, getString(R.string.general_error))
            }
            .doOnRequest {
                AppExecutorsService.mainThread().execute {
                    btnLogin.isEnabled = false
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.show(it.message)
                    if (it.success) {
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                        }, 1000)
                    } else {
                        btnLogin.isEnabled = true
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                        }, 3000)
                    }
                }
            }


    }


    private fun loadData() {
        progressDialog.loading()
        val userData = PreferenceManagerService.retrieveUserData()
        fieldFullName.setText(userData.fullName)
        fieldPhone.setText(userData.phone)

        Picasso
            .get()
            .load(userData.imageUrl)
            .placeholder(R.drawable.account_on)
            .into(profile_picture)

        AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 1000)
    }

    private fun isValidFields(): Boolean {

        if (fieldFullName.text.isEmpty()) {
            fieldFullName.requestFocus()
            fieldFullName.error = getString(R.string.fill_field_please)
            return false
        } else {
            val isValidFullName = Pattern
                .compile(getString(R.string.full_name_regex))
                .matcher(fieldFullName.text.toString())
                .matches()
            if (!isValidFullName || fieldFullName.text.length < 5) {
                fieldFullName.requestFocus()
                fieldFullName.error = getString(R.string.valid_field_please)
                return false
            }
        }



        if (fieldPhone.text.isEmpty()) {
            fieldPhone.requestFocus()
            fieldPhone.error = getString(R.string.fill_field_please)
            return false
        } else {
            val isValidPhone = Pattern
                .compile(getString(R.string.phone_number_regex))
                .matcher(StringUtils.getUnmaskedPhone(fieldPhone))
                .matches()

            if (!isValidPhone) {
                fieldPhone.requestFocus()
                fieldPhone.error = getString(R.string.valid_field_please)
                return false
            }
        }

        return true
    }

}