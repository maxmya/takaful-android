package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsClient
import com.dawa.user.lists.adapters.HomeMedicationsAdapter
import com.dawa.user.network.data.MedicationsMock
import com.dawa.user.network.data.Pageable
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_home.*

class HomeFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())

        val gridLayout = GridLayoutManager(requireActivity(), 2)
        medications_list.layoutManager = gridLayout
        getListOfMedications()
    }


    private fun getListOfMedications() {
        RetrofitClient
            .INSTANCE
            .listMedications()
            .onErrorReturn {
                Pageable()
            }
            .doOnRequest {
                AppExecutorsClient.mainThread().execute {
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsClient.mainThread().execute {
                    if (it.pagination == null) {
                        progressDialog.generalError()
                    } else {
                        medications_list.adapter = HomeMedicationsAdapter(it.pageAbleList)
                    }
                    AppExecutorsClient.handlerDelayed({
                        progressDialog.dismiss()
                    }, 1000)
                }
            }
    }


}