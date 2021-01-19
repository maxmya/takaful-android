package com.dawa.user.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.ui.dialogs.AuthCodeDialog
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.layout_forget_password.*
import java.util.concurrent.TimeUnit

private const val TAG = "ForgetPasswordFragment"

class ForgetPasswordFragment : Fragment(R.layout.layout_forget_password) {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var phoneNumber: String
    lateinit var tempView: View


    private val verificationCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                }
                Log.d(TAG, "onVerificationCompleted: verified from itself")

            }

            override fun onVerificationFailed(exp: FirebaseException) {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                }
                Log.e(TAG, "onVerificationFailed: ", exp)
            }

            override fun onCodeSent(verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                AppExecutorsService.handlerDelayed({
                    val authCodeFromDialog = object : RegistrationFragment.AuthCodeFromDialog {
                        override fun authCode(code: String) {
                            AppExecutorsService.mainThread().execute {
                                progressDialog.dismiss()
                            }
                            val cred = PhoneAuthProvider.getCredential(verificationId, code)
                            FirebaseAuth.getInstance().signInWithCredential(cred)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        toResetPassword(tempView, phoneNumber)
                                    } else {
                                        progressDialog.show("حدث خطأ ما , لم يتم تأكيد رقم الهاتف")
                                        AppExecutorsService.handlerDelayed({ progressDialog.dismiss() },
                                                2000)
                                    }
                                }
                        }
                    }
                    AuthCodeDialog(requireActivity(), authCodeFromDialog)

                }, 2000)
                Log.d(TAG, "onCodeSent: code sent and it's $verificationId and token is ${token}")
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                AppExecutorsService.mainThread().execute {
                    progressDialog.dismiss()
                }
                super.onCodeAutoRetrievalTimeOut(verificationId)
                progressDialog.show("حدث خطأ ما , لم يتم تأكيد رقم الهاتف")
                AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = MessageProgressDialog(requireActivity())

        tempView = verify_btn
        verify_btn.setOnClickListener {
            if (phoneText.text.isBlank()) {
                Toast.makeText(requireContext(),
                        "Please Enter The Phone Number",
                        Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendPhoneVerification(phoneNumber = phoneText.text.toString())
        }


    }

    private fun sendPhoneVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder().setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(requireActivity())
            .setCallbacks(verificationCallback).build()
        this.phoneNumber = phoneNumber
        PhoneAuthProvider.verifyPhoneNumber(options)
        progressDialog.loading()
    }

    private fun toResetPassword(view: View, phoneNumber: String) {
        val toRest = ForgetPasswordFragmentDirections.toResetPassword(phoneNumber)
        Navigation.findNavController(view).navigate(toRest)
    }

}