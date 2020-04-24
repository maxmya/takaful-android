package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.dawa.user.R
import com.dawa.user.lists.adapters.HomeMedicationsAdapter
import com.dawa.user.network.data.MedicationsMock
import kotlinx.android.synthetic.main.layout_home.*

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayout = GridLayoutManager(requireActivity(), 2)
        medications_list.layoutManager = gridLayout
        medications_list.adapter =
            HomeMedicationsAdapter(getMockData())

    }


    fun getMockData(): List<MedicationsMock> = mutableListOf(
        MedicationsMock(1, "الدواء الاول", 1.2, 1.2, "", "المعادي"),
        MedicationsMock(2, "الدواء الثاني", 1.2, 1.2, "", "دار السلام"),
        MedicationsMock(3, "الدواء الثالث", 1.2, 1.2, "", "الجيزه"),
        MedicationsMock(4, "الدواء الرابع", 1.2, 1.2, "", "الهرم"),
        MedicationsMock(5, "الدواء الخامس", 1.2, 1.2, "", "حلوان"),
        MedicationsMock(6, "الدواء السادس", 1.2, 1.2, "", "فيصل"),
        MedicationsMock(7, "الدواء السابع", 1.2, 1.2, "", "المهندسين"),
        MedicationsMock(8, "الدواء الثامن", 1.2, 1.2, "", "الزمالك")
    )


}