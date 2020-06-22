package com.dawa.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.network.data.UserPreservationDTO
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_item_midication.view.linearLayout4
import kotlinx.android.synthetic.main.layout_item_midication.view.medication_location_short
import kotlinx.android.synthetic.main.layout_item_midication.view.medication_name
import kotlinx.android.synthetic.main.layout_item_preservation.view.*


class PreservationAdapter : RecyclerView.Adapter<PreservationViewHolder>() {

    private val preservationList = mutableListOf<UserPreservationDTO>()

    fun add(listOfUserPreservation: List<UserPreservationDTO>) {
        preservationList.addAll(listOfUserPreservation)
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
            .into(holder.itemView.linearLayout4)

        holder.itemView.medication_name.text = currentPreservation.medicine?.name
        holder.itemView.medication_location_short.text = currentPreservation.medicine?.addressTitle
        holder.itemView.medicationUserPhoneNumber.text = currentPreservation.medicine?.userDTO?.phone

    }

}

class PreservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)