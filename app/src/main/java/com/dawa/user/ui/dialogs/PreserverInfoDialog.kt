package com.dawa.user.ui.dialogs

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.dawa.user.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PreserverInfoDialog(private var activity: Activity) {


    private val alert: AlertDialog.Builder = AlertDialog.Builder(activity, R.style.DawaDialogStyle)
    private var name: TextView
    private var phone: TextView
    private var image: CircleImageView
    private var dialog: AlertDialog

    init {
        val mView: View = activity.layoutInflater.inflate(R.layout.layout_preserver_dialog, null)
        name = mView.findViewById(R.id.name) as TextView
        phone = mView.findViewById(R.id.phone) as TextView
        image = mView.findViewById(R.id.profile_picture) as CircleImageView


        alert.setView(mView)
        alert.setCancelable(true)
        dialog = this.alert.create();
    }


    fun show(dialogUser: DialogUser) {
        if (dialog.isShowing) {
            setupDialog(dialogUser)
        } else {
            setupDialog(dialogUser)
            dialog.show()
        }
    }


    private fun setupDialog(dialogUser: DialogUser) {
        name.text = dialogUser.name
        phone.text = dialogUser.phone
        Picasso.get().load(dialogUser.imageUrl).placeholder(R.drawable.blank_user).fit()
            .centerCrop().into(image)

    }


}

data class DialogUser(val name: String, val phone: String, val imageUrl: String)