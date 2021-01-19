package com.dawa.user.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.MedicationsDTO
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.DialogUser
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.ui.dialogs.PreserverInfoDialog
import com.himangi.imagepreview.ImagePreviewActivity
import com.himangi.imagepreview.PreviewFile
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_my_medication.view.*

class MyMedicationAdapter constructor(val progressDialog: MessageProgressDialog,
                                      val activity: Activity) :
    RecyclerView.Adapter<MyMedicationViewHolder>() {


    private val medicationsList = mutableListOf<MedicationsDTO>()

    fun add(listOfMedication: List<MedicationsDTO>) {
        medicationsList.addAll(listOfMedication)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMedicationViewHolder {
        return MyMedicationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_my_medication, parent, false))
    }

    override fun getItemCount(): Int {
        return medicationsList.size
    }

    override fun onBindViewHolder(holder: MyMedicationViewHolder, position: Int) {

        val medication = medicationsList[position]

        Picasso.get().load(medication.imageUrl).fit().centerCrop()
            .placeholder(R.drawable.medication).fit().centerCrop().into(holder.itemView.image)

        val previewFiles: ArrayList<PreviewFile> = ArrayList()
        previewFiles.add(PreviewFile(medication.imageUrl, ""))

        holder.itemView.image.setOnClickListener {
            val intent = Intent(activity, ImagePreviewActivity::class.java)
            intent.putExtra(ImagePreviewActivity.IMAGE_LIST, previewFiles)
            activity.startActivity(intent)
        }


        holder.itemView.name.text = medication.name
        holder.itemView.address.text = medication.addressTitle

        if (medication.preserver != null) {
            holder.itemView.preserved.visibility = View.VISIBLE
            holder.itemView.preserved.setOnClickListener {
                RetrofitClient.INSTANCE.getMyMedicationPreserver(medication.id).onErrorReturn {
                    it.printStackTrace()
                    ResponseWrapper(false, "error", null)
                }.doOnRequest {
                    AppExecutorsService.mainThread().execute {
                        progressDialog.loading()
                    }
                }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
                    AppExecutorsService.mainThread().execute {
                        progressDialog.dismiss()
                        if (it.success && it.data != null) {
                            PreserverInfoDialog(activity).show(DialogUser(it.data.fullName,
                                    it.data.phone,
                                    it.data.pictureUrl))
                        } else {
                            Toast.makeText(activity,
                                    "cannot get preserver info",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            holder.itemView.preserved.visibility = View.INVISIBLE
        }

        holder.itemView.delete.setOnClickListener {
            RetrofitClient.INSTANCE.deleteMyMedication(medication.id).onErrorReturn {
                it.printStackTrace()
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
                        // reload
                        medicationsList.removeAt(position)
                        notifyDataSetChanged()
                    } else {
                        progressDialog.generalError()
                    }
                    AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
                }
            }
        }

    }
}


class MyMedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)