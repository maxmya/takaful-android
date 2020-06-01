package com.dawa.user.ui.dialogs

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dawa.user.R
import com.dawa.user.ui.fragments.RegistrationFragment
import com.google.android.material.textfield.TextInputEditText

class AuthCodeDialog(private var activity: Activity,
                     private var autCodeCallBack: RegistrationFragment.AuthCodeFromDialog) {

    private val alert: AlertDialog.Builder =
        AlertDialog.Builder(activity, R.style.MProgressDialogStyle)

    private var codeView: TextInputEditText
    private var dialog: AlertDialog

    init {
        val mView: View = activity.layoutInflater.inflate(R.layout.layout_auth_dialog, null)
        codeView = mView.findViewById(R.id.code_view)
        alert.setView(mView)
        alert.setCancelable(false)
        dialog = this.alert.create()
        mView.findViewById<Button>(R.id.verify_btn).setOnClickListener {
            if (codeView.text != null && codeView.text.toString()
                    .isNotEmpty() || codeView.text.toString().isNotBlank()) {
                autCodeCallBack.authCode(codeView.text.toString())
                dialog.dismiss()
            } else {
                Toast.makeText(activity, "من فضلك قم بإدخال رمز التحقق", Toast.LENGTH_SHORT).show()
            }
        }

        show()
    }

    private fun show() {
        if (dialog.isShowing) {
            // do nothing
        } else {
            dialog.show()
        }
    }


}