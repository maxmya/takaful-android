package com.takaful.user.lists.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.takaful.user.R
import com.takaful.user.network.data.MedicationsMock
import kotlinx.android.synthetic.main.layout_item_midication.view.*


class HomeMedicationsAdapter(private val medicationsList: List<MedicationsMock>) :
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
            .load(R.drawable.medication)
            .placeholder(R.drawable.medication)
            .into(holder.itemView.linearLayout4)

        holder.itemView.medication_name.text = currentMedication.name
        holder.itemView.medication_location_short.text = currentMedication.addressTitle
    }
}

class HomeMedicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)