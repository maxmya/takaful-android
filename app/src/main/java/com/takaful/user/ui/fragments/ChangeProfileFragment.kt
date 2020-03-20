package com.takaful.user.ui.fragments

import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.takaful.user.R
import com.takaful.user.ui.dialogs.MessageProgressDialog
import com.takaful.user.utils.StringUtils
import kotlinx.android.synthetic.main.layout_registration.*
import java.util.regex.Pattern

class ChangeProfileFragment :Fragment() {
    lateinit var progressDialog: MessageProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_change_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        if (fieldPassword.text.isEmpty()) {
            fieldPassword.requestFocus()
            fieldPassword.error = getString(R.string.fill_field_please)
            return false
        } else if (fieldPassword.text.length < 6) {
            fieldPassword.requestFocus()
            fieldPassword.error = getString(R.string.valid_password_length)
            return false
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