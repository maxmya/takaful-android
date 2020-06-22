package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dawa.user.R
import com.dawa.user.adapters.MyMedicationAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_medication.*

class MyMedicationsFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var myMedicationAdapter: MyMedicationAdapter


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        setupList()
    }

    private fun setupList() {
        myMedicationAdapter = MyMedicationAdapter(progressDialog,requireActivity())
        my_medications_list.adapter = myMedicationAdapter
        my_medications_list.layoutManager = LinearLayoutManager(requireContext())

        RetrofitClient.INSTANCE.listMyMedications().onErrorReturn {
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
                    if (it.data != null) myMedicationAdapter.add(it.data)
                } else {
                    progressDialog.generalError()
                }
                AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
            }
        }
    }

}