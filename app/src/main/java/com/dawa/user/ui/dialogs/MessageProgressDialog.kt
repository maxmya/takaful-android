package com.dawa.user.ui.dialogs

import android.app.Activity
import android.view.View

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dawa.user.R


class MessageProgressDialog(private var activity: Activity) {

    private val alert: AlertDialog.Builder =
        AlertDialog.Builder(activity, R.style.MProgressDialogStyle)
    private var messageView: TextView
    private var dialog: AlertDialog

    init {
        val mView: View = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        messageView = mView.findViewById(R.id.message) as TextView
        alert.setView(mView)
        alert.setCancelable(false)
        dialog = this.alert.create();
    }

    fun show(message: String) {
        if (dialog.isShowing) {
            changeMessage(message)
        } else {
            messageView.text = message
            dialog.show()
        }
    }

    fun loading() {
        show(activity.getString(R.string.loading))
    }

    fun generalError() {
        show(activity.getString(R.string.general_error))
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private fun changeMessage(newMessage: String?) {
        if (dialog.isShowing)
            messageView.text = newMessage
    }


}