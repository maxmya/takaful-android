package com.dawa.user.lists.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.dawa.user.R
import com.dawa.user.network.data.MedicationsDTO
import kotlinx.android.synthetic.main.layout_item_midication.view.*


class HomeMedicationsAdapter(private val medicationsList: List<MedicationsDTO>) :
    RecyclerView.Adapter<HomeMedicationsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMedicationsViewHolder {
        return HomeMedicationsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_midication, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return medicationsList.size
    }

    override fun onBindViewHolder(holder: HomeMedicationsViewHolder, position: Int) {

        val currentMedication = medicationsList[position]

        Picasso
            .get()
            .load(currentMedication.imageUrl)
            .placeholder(R.drawable.medication)
            .into(holder.itemView.linearLayout4)

        holder.itemView.medication_name.text = currentMedication.name
        holder.itemView.medication_location_short.text = currentMedication.addressTitle
    }
}

class HomeMedicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)