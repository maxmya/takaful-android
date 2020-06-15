package com.dawa.user.handlers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


const val OPERATION_CAPTURE_PHOTO = 1
const val OPERATION_CHOOSE_PHOTO = 2

class TakePhotoService constructor(val fragment: Fragment, val context: Context) {


    fun openGallery() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        fragment.startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }


    fun resultForImageGallery(imageIntent: Intent, imageView: ImageView): MultipartBody.Part {
        val imageURI = imageIntent.data
        Picasso.get().load(imageURI).fit().centerCrop().placeholder(R.drawable.logo).into(imageView)
        return getImageAsMultiPart(imageURI!!)
    }


    fun capturePhoto(): Uri {
        val capturedImage =
            File(context.applicationContext.externalCacheDir, "${UUID.randomUUID()}.jpg")
        capturedImage.createNewFile()
        val imageURI = if (Build.VERSION.SDK_INT >= 24) {
            context.applicationContext.let {
                FileProvider.getUriForFile(it, "com.dawa.user.fileprovider", capturedImage)
            }
        } else {
            Uri.fromFile(capturedImage)
        }
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
        fragment.startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
        return imageURI
    }


    fun resultForImageCapture(imageURI: Uri, imageView: ImageView): MultipartBody.Part {
        Picasso.get().load(imageURI).fit().centerCrop().placeholder(R.drawable.logo).into(imageView)
        return getImageAsMultiPart(imageURI)
    }


    private fun getImageAsMultiPart(imageURI: Uri): MultipartBody.Part {
        val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
        val inputStream = context.contentResolver.openInputStream(imageURI)
        FileUtils.copyToFile(inputStream, file);
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name.trim(), requestFile)
    }


}
