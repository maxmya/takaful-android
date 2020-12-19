package com.dawa.user.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.data.UserPreservationDTO
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_item_preservation.view.*
import okhttp3.internal.notifyAll


class PreservationAdapter (val progressDialog: MessageProgressDialog,val activity: Activity): RecyclerView.Adapter<PreservationViewHolder>() {

    private val preservationList = mutableListOf<UserPreservationDTO>()
    private val preservationListAfterDeletion = mutableListOf<UserPreservationDTO>()

    fun add(listOfUserPreservation: List<UserPreservationDTO>) {
        preservationList.addAll(listOfUserPreservation)
        notifyDataSetChanged()
    }

    private fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        preservationList.removeAt(viewHolder.adapterPosition)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreservationViewHolder {
        return PreservationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_preservation, parent, false))
    }

    override fun getItemCount(): Int {
        return preservationList.size
    }

    override fun onBindViewHolder(holder: PreservationViewHolder, position: Int) {

        val currentPreservation = preservationList[position]

        Picasso.get().load(currentPreservation.medicine?.imageUrl).placeholder(R.drawable.medication)
            .into(holder.itemView.medImage)
        holder.itemView.id= currentPreservation.id!!
        holder.itemView.preserve_medication_name.text = currentPreservation.medicine?.name
        holder.itemView.preserve_medication_location_short.text = currentPreservation.medicine?.addressTitle
        holder.itemView.medicationUserPhoneNumber.text = currentPreservation.medicine?.userDTO?.phone
        holder.itemView.deletePreserve.setOnClickListener{
            deletePreservation(currentPreservation.id,position)
        }
    }


    private fun deletePreservation(id: Int,
                                   position: Int) {

        RetrofitClient.INSTANCE.deletePreservation(id).onErrorReturn {
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
                        preservationList.removeAt(position)
                        notifyDataSetChanged()
                        Toast.makeText(activity,
                                "تم مسح حجز الدواء بنجاح",
                                Toast.LENGTH_LONG).show()

                } else {
                    progressDialog.generalError()
                }
                AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
            }
        }
    }
}

class PreservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)