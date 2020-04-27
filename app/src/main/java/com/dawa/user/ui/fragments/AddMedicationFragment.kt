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
import com.dawa.user.handlers.AppExecutorsClient
import com.dawa.user.network.data.MedicineCategoryDTO
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_add_medication.*
import kotlinx.android.synthetic.main.layout_change_profile.*
import java.io.File


private const val OPERATION_CAPTURE_PHOTO = 1
private const val OPERATION_CHOOSE_PHOTO = 2


class AddMedicationFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private var mUri: Uri? = null
    private var imagePath: String? = null

    lateinit var progressDialog: MessageProgressDialog
    lateinit var categoryList: List<MedicineCategoryDTO>

    var selectedCategoryId = 0


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
        loadCategories()


        add_image.setOnClickListener {
            showPictureDialog()
        }


        add_med.setOnClickListener {
            progressDialog.loading()

            AppExecutorsClient.handlerDelayed({
                progressDialog.generalError()
                AppExecutorsClient.handlerDelayed({ progressDialog.dismiss() }, 1000)
            }, 5000)
        }

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
                    it, "com.dawa.user.fileprovider",
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
            profile_picture?.setImageBitmap(bitmap)
        }

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
                    profile_picture!!.setImageBitmap(bitmap)
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    handleImageOnKitkat(data)
                }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        val pictureDialogItems = arrayOf(
            "Select photo from gallery",
            "Capture photo from camera"
        )
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


    private fun loadCategories() {
        RetrofitClient
            .INSTANCE
            .listMedicationsCategories()
            .onErrorReturn { mutableListOf() }
            .doOnRequest {
                AppExecutorsClient.mainThread().execute {
                    progressDialog.loading()
                }
            }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsClient.mainThread().execute {
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

}