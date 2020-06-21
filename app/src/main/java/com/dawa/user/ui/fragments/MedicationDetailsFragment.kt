package com.dawa.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ErrorClass
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.ui.dialogs.YesNoDialog
import com.dawa.user.ui.dialogs.YesNoFlow
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_medication_details.*


class MedicationDetailsFragment : Fragment() {


    private lateinit var progressDialog: MessageProgressDialog
    private lateinit var yesNoDialog: YesNoDialog

    private val yesNoFlow = object : YesNoFlow {

        override fun yes() {
            Toast.makeText(requireContext(), "Yes", Toast.LENGTH_SHORT).show()
        }

        override fun no() {
            Toast.makeText(requireContext(), "No", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_medication_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())
        yesNoDialog = YesNoDialog(requireActivity(), yesNoFlow)

        requireArguments().let {
            val medicationId = MedicationDetailsFragmentArgs.fromBundle(it).medicationId
            loadMedicationDetails(medicationId)
            reserveButton.setOnClickListener {
                yesNoDialog.show("هل انت متاكد ايها الاحمق")
//                reserveMedicineWithNetworkCall(medicationId)
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

                    val imageUrlArr = arrayListOf<String>(it.imageUrl)
                    medication_image_det.setOnClickListener {

                        val fullImageIntent =
                            Intent(requireActivity(), FullScreenImageViewActivity::class.java)
                        fullImageIntent.putExtra(FullScreenImageViewActivity.URI_LIST_DATA,
                                imageUrlArr)
                        fullImageIntent.putExtra(FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS,
                                0)
                        startActivity(fullImageIntent)

                    }

                    medicationName.text = it.name
                    personName.text = it.userDTO!!.fullName
                    address.text = it.addressTitle

                }
            }
        }

    }

    private fun reserveMedicineWithNetworkCall(id: Int) {

        RetrofitClient.INSTANCE.medicinePreservation(id).onErrorReturn {
            ErrorClass(false, getString(R.string.general_error))
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                reserveButton.isEnabled = false
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                if (it.success) {
                    AppExecutorsService.handlerDelayed({
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(),
                                getString(R.string.reserved),
                                Toast.LENGTH_LONG).show()
                    }, 1000)
                } else {
                    reserveButton.isEnabled = true
                    AppExecutorsService.handlerDelayed({
                        progressDialog.dismiss()
                        if (it.message == "alreadyPreserved") {
                            Toast.makeText(requireContext(),
                                    getString(R.string.alreadyPreserved),
                                    Toast.LENGTH_LONG).show()
                        }
                    }, 3000)
                }
            }
        }


    }

}