package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.adapters.HomeMedicationsAdapter
import com.dawa.user.network.data.Pageable
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_home.*

class HomeFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var medicationsAdapter: HomeMedicationsAdapter

    var hasMore = false
    var page = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        page = 1
        return inflater.inflate(R.layout.layout_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())

        medicationsAdapter = HomeMedicationsAdapter()
        val gridLayout = GridLayoutManager(requireActivity(), 2)
        medications_list.layoutManager = gridLayout
        medications_list.adapter = medicationsAdapter

        medications_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (gridLayout.findLastVisibleItemPosition() == gridLayout.itemCount - 1 && hasMore) {
                    page++;
                    getListOfMedications(page)
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        getListOfMedications(page)
    }


    private fun getListOfMedications(pageNumber: Int) {
        RetrofitClient.INSTANCE.listMedications(pageNumber.toString()).onErrorReturn {
            Pageable()
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
                AppExecutorsService.mainThread().execute {
                    if (it.pagination == null) {
                        progressDialog.generalError()
                    } else {
                        hasMore = it.next
                        medicationsAdapter.add(it.pageAbleList)
                    }
                    AppExecutorsService.handlerDelayed({
                        progressDialog.dismiss()
                    }, 1000)
                }
            }
    }


}