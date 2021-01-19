package com.dawa.user.ui.fragments

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.dawa.user.R

class ResetPasswordFragment : Fragment(R.layout.layout_reset_password) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
    }
}