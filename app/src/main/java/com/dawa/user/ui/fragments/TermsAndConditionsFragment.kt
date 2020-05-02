package com.dawa.user.ui.fragments

import android.app.Activity
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
import com.dawa.user.handlers.TakePhotoService
import kotlinx.android.synthetic.main.layout_terms_conditions.*

class TermsAndConditionsFragment : Fragment() {

    lateinit var imageUri: Uri
    lateinit var photoService: TakePhotoService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_terms_conditions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoService = TakePhotoService(
            this,
            requireContext()
        )

        capture.setOnClickListener {
             photoService.openGallery()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "ON RESULT")
        if (resultCode == Activity.RESULT_OK) {
            val multiplatform = photoService.resultForImageGallery(data!!, image)
            Log.d("TAG", multiplatform.toString())
        } else {
            Toast.makeText(requireContext(), "$resultCode error", Toast.LENGTH_LONG).show()
        }
    }

}