package com.dawa.user.ui.dialogs

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.dawa.user.R

class YesNoDialog(private var activity: Activity, private var yesNoFlow: YesNoFlow) {

    private val alert: AlertDialog.Builder = AlertDialog.Builder(activity, R.style.DawaDialogStyle)
    private var messageView: TextView
    private var dialog: AlertDialog
    private var yesBtn: Button
    private var noBtn: Button

    init {
        val mView: View = activity.layoutInflater.inflate(R.layout.layout_yes_no_dialog, null)
        messageView = mView.findViewById(R.id.message) as TextView
        yesBtn = mView.findViewById(R.id.yes) as Button
        noBtn = mView.findViewById(R.id.no) as Button

        alert.setView(mView)
        alert.setCancelable(false)
        dialog = this.alert.create()

        yesBtn.setOnClickListener {
            dialog.dismiss()
            yesNoFlow.yes()
        }
        noBtn.setOnClickListener {
            dialog.dismiss()
            yesNoFlow.no()
        }

    }

    fun show(message: String) {
        if (dialog.isShowing) {
            changeMessage(message)
        } else {
            messageView.text = message
            dialog.show()
        }
    }

    private fun changeMessage(newMessage: String?) {
        if (dialog.isShowing) messageView.text = newMessage
    }

}


public interface YesNoFlow {
    fun yes()
    fun no()
}