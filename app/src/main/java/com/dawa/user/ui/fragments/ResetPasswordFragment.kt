package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.network.data.ChangePasswordRequest
import com.dawa.user.network.data.ResponseWrapper
import com.dawa.user.network.data.UserTokenRequest
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_reset_password.*

class ResetPasswordFragment : Fragment(R.layout.layout_reset_password) {

    lateinit var progressDialog: MessageProgressDialog
    lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
        progressDialog = MessageProgressDialog(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireArguments().let {
            this.phoneNumber =
                ResetPasswordFragmentArgs.fromBundle(it).phoneNumber.replace("+", "00")
        }

        change_password.setOnClickListener {
            if (password.text.toString().isEmpty() || repassword.text.toString().isEmpty()) {
                progressDialog.show("من فضلك قم بمئ الخانات المطلوبه")
                AppExecutorsService.handlerDelayed({
                    progressDialog.dismiss()
                }, 1000)
            }
            if (password.text.toString() != repassword.text.toString()) {
                progressDialog.show("رقم السر غير مطابق ,تأكد من ادخال رقم السر وتكراره بصوره صحيحه")
                AppExecutorsService.handlerDelayed({
                    progressDialog.dismiss()
                }, 1000)
            }
            changePassword(change_password, password.text.toString())
        }
    }

    private fun changePassword(view: View, newPassword: String) {
        val changePasswordRequest = ChangePasswordRequest(phoneNumber, newPassword)
        RetrofitClient.INSTANCE.changeUserPassword(changePasswordRequest).onErrorReturn {
            if (it.message != null) {
                ResponseWrapper(false, it.message!!, false)
            } else {
                ResponseWrapper(false, "error occurred", false)
            }
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            if (it.success && it.data == true) {
                val loginRequest = UserTokenRequest(phoneNumber, newPassword)
                openLogin(view, loginRequest)
            } else {
                AppExecutorsService.mainThread().execute {
                    progressDialog.generalError()
                }
            }
            AppExecutorsService.handlerDelayed({
                progressDialog.dismiss()
            }, 1000)
        }
    }

    private fun openLogin(view: View, userLoginData: UserTokenRequest?) {
        val toLogin = ResetPasswordFragmentDirections.toLogin(userLoginData)
        Navigation.findNavController(view).navigate(toLogin)
    }
}