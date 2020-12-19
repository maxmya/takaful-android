package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.adapters.PreservationAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.ui.dialogs.YesNoDialog
import com.dawa.user.ui.dialogs.YesNoFlow
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_item_preservation.*
import kotlinx.android.synthetic.main.layout_preservation.*


class PreservationsFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var preservationAdapter: PreservationAdapter
    private lateinit var yesNoDialog: YesNoDialog



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_preservation, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())
        listPreservation()

    }
    private fun listPreservation() {
        preservationAdapter = PreservationAdapter(progressDialog,requireActivity())
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

    /*var mIth: Unit = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT) {


        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            yesNoDialog.show("هل انت متاكد من مسح حجز هذا الدواء ؟")
            if(deletePreservation) {
                deletePreservation(viewHolder.itemId.toInt(), viewHolder)
            }
        }
    }).attachToRecyclerView(preservation_List)*/






}