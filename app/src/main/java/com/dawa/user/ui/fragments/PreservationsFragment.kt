package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dawa.user.R
import com.dawa.user.adapters.PreservationAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.*
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_preservation.*


class PreservationsFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var preservationAdapter: PreservationAdapter



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_preservation, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())
        setupList()
    }
    private fun setupList() {
        preservationAdapter = PreservationAdapter()
        preservation_List.adapter = preservationAdapter
        preservation_List.layoutManager = LinearLayoutManager(requireContext())

        RetrofitClient.INSTANCE.listUserPreservations().onErrorReturn {
            if (it.message != null) {
                ResponseWrapper(false, it.message!!, null)
            } else {
                ResponseWrapper(false, "error occurred", null)
            }
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                if (it.success) {
                    progressDialog.dismiss()
                    if (it.data != null) preservationAdapter.add(it.data)
                } else {
                    progressDialog.generalError()
                }
                AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
            }
        }
    }



}