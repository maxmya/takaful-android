package com.takaful.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.takaful.user.R
import com.takaful.user.handlers.AppExecutorsClient
import com.takaful.user.handlers.PreferenceManger
import com.takaful.user.ui.HomeActivity
import com.takaful.user.ui.dialogs.MessageProgressDialog
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_change_profile.*

class ProfileFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_change_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        loadData()
    }

    private fun loadData() {
        progressDialog.loading()
        val userData = PreferenceManger.retrieveUserData();
        fieldFullName.setText(userData.fullName)
        fieldPhone.setText(userData.phone)
        AppExecutorsClient.handlerDelayed({ progressDialog.dismiss() }, 1000)
    }

    private fun isValidFields(): Boolean {
        return true
    }

}