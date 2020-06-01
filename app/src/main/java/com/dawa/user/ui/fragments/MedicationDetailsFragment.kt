package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ErrorClass
import com.dawa.user.network.data.UserRegisterResponse
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_medication_details.*
import java.lang.Error


class MedicationDetailsFragment : Fragment() {


    private lateinit var progressDialog: MessageProgressDialog


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_medication_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())

        requireArguments().let {
            val medicationId = MedicationDetailsFragmentArgs.fromBundle(it).medicationId
            loadMedicationDetails(medicationId)
            reserveButton.setOnClickListener{
                reserveMedicineWithNetworkCall(medicationId)
            }
        }
    }


    private fun loadMedicationDetails(medicationId: Int) {
        RetrofitClient.INSTANCE.listMedicationDetails(medicationId).onErrorReturn {
                it.printStackTrace()
                null
            }.doOnRequest {
                AppExecutorsService.mainThread().execute {
                    progressDialog.loading()
                }
            }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                    if (it != null) {
                        Picasso.get().load(it.imageUrl).fit().centerCrop()
                            .placeholder(R.drawable.medication).into(medication_image_det)
                        medicationName.text = it.name
                        personName.text = it.userDTO!!.fullName
                        address.text = it.addressTitle

                    }
                }
            }

    }

    private fun reserveMedicineWithNetworkCall(id: Int) {

        RetrofitClient
            .INSTANCE
            .medicinePreservation(id)
            .onErrorReturn {
                ErrorClass(false, getString(R.string.general_error))
            }
            .doOnRequest {
                AppExecutorsService.mainThread().execute {
                    reserveButton.isEnabled = false
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsService.mainThread().execute {
                    if (it.success) {
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.reserved),
                                    Toast.LENGTH_LONG
                            ).show()
                        }, 1000)
                    } else {
                        reserveButton.isEnabled = true
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                            if(it.message == "alreadyPreserved"){
                                Toast.makeText(
                                        requireContext(),
                                        getString(R.string.alreadyPreserved),
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }, 3000)
                    }
                }
            }


    }

}