package com.dawa.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dawa.user.R
import com.dawa.user.adapters.HomeMedicationsAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService
import com.dawa.user.network.data.Pageable
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.HomeActivity
import com.dawa.user.ui.UserActivity
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

        if (PreferenceManagerService.retrieveToken().isEmpty()) {
            signIn.visibility = View.VISIBLE
            signIn.setOnClickListener {
                val intent = Intent(requireContext(), UserActivity::class.java)
                requireActivity().startActivity(intent)
            }
        } else {
            signIn.visibility = View.GONE
        }

        medicationsAdapter = HomeMedicationsAdapter()
        val gridLayout = GridLayoutManager(requireActivity(), 2)
        medications_list.layoutManager = gridLayout
        medications_list.adapter = medicationsAdapter
        val query = searchView.query!!.toString()
        getListOfMedications(page.toString(), null)
        medications_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (gridLayout.findLastVisibleItemPosition() == gridLayout.itemCount - 1 && hasMore) {
                    page++
                    if (query.isEmpty()) {
                        getListOfMedications(page.toString(), null)
                    } else {
                        getListOfMedications("", query)
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isEmpty()) {
                    getListOfMedications(page.toString(), null)
                } else {
                    getListOfMedications("", query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        my_medications.setOnClickListener {
            if (PreferenceManagerService.retrieveToken().isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please Register Account To Be Able To View Your Medications",
                        Toast.LENGTH_LONG).show();
            } else {
                val toMyMedications = HomeFragmentDirections.toMyMedications()
                Navigation.findNavController(it).navigate(toMyMedications)
            }
        }

    }


    private fun getListOfMedications(pageNumber: String?, query: String?) {
        RetrofitClient.INSTANCE.listMedications(query, "20", pageNumber.toString()).onErrorReturn {
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
                    page = it.pagination!!.currentPage
                    hasMore = it.next
                    medicationsAdapter.add(it.pageAbleList, query)
                }
                AppExecutorsService.handlerDelayed({
                    progressDialog.dismiss()
                }, 1000)
            }
        }
    }

}