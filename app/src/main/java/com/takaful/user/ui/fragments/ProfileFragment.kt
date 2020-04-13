package com.takaful.user.ui.fragments

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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.takaful.user.R
import com.takaful.user.handlers.AppExecutorsClient
import com.takaful.user.handlers.PreferenceManger
import com.takaful.user.network.data.*
import com.takaful.user.network.retrofit.RetrofitClient
import com.takaful.user.ui.dialogs.MessageProgressDialog
import com.takaful.user.utils.StringUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_change_profile.*
import kotlinx.android.synthetic.main.layout_change_profile.fieldFullName
import kotlinx.android.synthetic.main.layout_change_profile.fieldPhone
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.regex.Pattern

private const val OPERATION_CAPTURE_PHOTO = 1
private const val OPERATION_CHOOSE_PHOTO = 2

private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {

    private var mUri: Uri? = null
    private var imagePath: String? = null

    lateinit var progressDialog: MessageProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_change_profile, container, false)
    }


    private fun capturePhoto() {
        val capturedImage =
            File(requireActivity().applicationContext.externalCacheDir, "My_Captured_Photo.jpg")
        if (capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if (Build.VERSION.SDK_INT >= 24) {
            requireActivity().applicationContext.let {
                FileProvider.getUriForFile(
                    it, "com.takaful.user.fileprovider",
                    capturedImage
                )
            }
        } else {
            Uri.fromFile(capturedImage)
        }

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
    }

    private fun openGallery() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun renderImage(imagePath: String?) {
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView?.setImageBitmap(bitmap)
        }

    }

    private fun removeImage() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Remove Photo")
        builder.setNegativeButton(R.string.no) { dialog, which ->

        }
        builder.setPositiveButton(R.string.yes) { dialog, which ->
            imageView.setImageBitmap(null)
            imagePath = null
            imageView.setImageResource(R.drawable.account_on)
        }
        builder.show()

    }

    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = uri?.let {
            requireActivity().applicationContext.contentResolver.query(
                it,
                null,
                selection,
                null,
                null
            )
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                imagePath = path
            }
            cursor.close()
        }
        return path!!
    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(requireActivity().applicationContext, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if (uri != null) {
                if ("com.android.providers.media.documents" == uri.authority) {
                    val id = docId.split(":")[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection
                    )
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(
                            "content://downloads/public_downloads"
                        ), java.lang.Long.valueOf(docId)
                    )
                    imagePath = getImagePath(contentUri, null)
                }
            }
        } else if (uri != null) {
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }
        }
        this.imagePath = imagePath
        renderImage(imagePath)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>
        , grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.permission_Denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(
                        mUri?.let {
                            requireActivity().applicationContext.contentResolver.openInputStream(it)
                        })
                    imageView!!.setImageBitmap(bitmap)
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    handleImageOnKitkat(data)
                }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> {
                    val checkSelfPermission = context?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                    if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                        //Requests permissions to be granted to this application at runtime
                        requireActivity().let {
                            ActivityCompat.requestPermissions(
                                it,
                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                            )
                        }

                    } else {
                        openGallery()
                    }
                }
                1 -> capturePhoto()
            }
        }
        pictureDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        loadData()
        StringUtils.maskPhoneField(fieldPhone)
        imageView.setOnClickListener {
            if (imagePath != null) {
                removeImage()
            }
        }
        imageView2.setOnClickListener { showPictureDialog() }
        editUserProfileAction()
    }


    private fun editUserProfileAction() {

        btnLogin.setOnClickListener {
            var multiPartFile: MultipartBody.Part? = null
            if (!isValidFields())
                return@setOnClickListener
            if (imagePath != null) {
                val alUri = arrayListOf<String>()
                alUri.add(imagePath.toString())
                multiPartFile = uploadFile(alUri)[0]
            }
            val profileRequest =
                ChangeProfileRequest(
                    StringUtils.getUnmaskedPhone(fieldPhone),
                    fieldFullName.text.toString().trim(),
                    StringUtils.getUnmaskedPhone(fieldPhone)
                )
            val json = Gson().toJson(profileRequest)
            val reqBody =
                RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
            val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                "body", reqBody.toString()
            )
            makeRegisterWithNetworkCall(body, multiPartFile)
        }
    }


    private fun makeRegisterWithNetworkCall(
        changeProfileBody: MultipartBody.Part,
        image: MultipartBody.Part?
    ) {

        RetrofitClient
            .INSTANCE
            .changeUserProfile(changeProfileBody, image)
            .onErrorReturn {
                ErrorClass(false, getString(R.string.general_error))
            }
            .doOnRequest {
                AppExecutorsClient.mainThread().execute {
                    btnLogin.isEnabled = false
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsClient.mainThread().execute {
                    progressDialog.show(it.message)
                    if (it.success) {
                        AppExecutorsClient.handlerDelayed({
                            progressDialog.dismiss()
                            navigateToHome(btnLogin)
                        }, 1000)
                    } else {
                        btnLogin.isEnabled = true
                        AppExecutorsClient.handlerDelayed({
                            progressDialog.dismiss()
                        }, 3000)
                    }
                }
            }


    }

    private fun uploadFile(selectedUris: java.util.ArrayList<String>): ArrayList<MultipartBody.Part> {
        val multiParts: ArrayList<MultipartBody.Part> = ArrayList()
        for (i in 0 until selectedUris.size) {
            // 1. Create File using image url (String)
            val file = File(selectedUris[i])
            println("file.name: " + file.name)
            // 2. Create requestBody by using multipart/form-data MediaType from file
            val requestFile: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            // 3. Finally, Create MultipartBody using MultipartBody.Part.createFormData
            val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image", file.name.trim(), requestFile
            )
            multiParts.add(body)
        }
        return multiParts
    }

    private fun navigateToHome(view: View) {
        val toHome = ProfileFragmentDirections.changeProfileToHome()
        Navigation.findNavController(view).navigate(toHome)

    }

    private fun loadData() {
        progressDialog.loading()
        val userData = PreferenceManger.retrieveUserData()
        Log.d(TAG, "")
        fieldFullName.setText(userData.fullName)
        fieldPhone.setText(userData.phone)
        AppExecutorsClient.handlerDelayed({ progressDialog.dismiss() }, 1000)
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