package com.takaful.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.takaful.user.R
import com.takaful.user.data.UserData
import com.takaful.user.network.data.UserProfileResponse
import com.takaful.user.network.data.UserTokenRequest
import com.takaful.user.network.retrofit.RetrofitClient
import com.takaful.user.ui.HomeActivity
import com.takaful.user.ui.dialogs.MessageProgressDialog
import com.takaful.user.handlers.AppExecutorsClient
import com.takaful.user.handlers.PreferenceManger
import com.takaful.user.utils.StringUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_login.fieldPassword
import kotlinx.android.synthetic.main.layout_login.fieldPhone

class LoginFragment : Fragment() {

    private val progressDialog = MessageProgressDialog(requireActivity())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StringUtils.maskPhoneField(fieldPhone)

        val statement = getString(R.string.register_a_new_account)
        val word = getString(R.string.create_account)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val toRegistrationAction = LoginFragmentDirections.toRegistration()
                Navigation.findNavController(textView).navigate(toRegistrationAction)
            }
        }

        StringUtils.makeUrlColoredSpan(statement, word, open_reg_screen, clickableSpan)

        requireArguments().let {
            val loginRequest = LoginFragmentArgs.fromBundle(it).userRequest
            if (loginRequest != null) {
                fieldPhone.setText(loginRequest.username)
                fieldPassword.setText(loginRequest.password)
                AppExecutorsClient.handlerDelayed({
                    progressDialog.loading()
                    loginByNetworkCall(loginRequest)
                }, 1000)
            } else {
                btnLogin.setOnClickListener {
                    if (!isValidFields())
                        return@setOnClickListener
                    loginByNetworkCall(
                        UserTokenRequest(
                            StringUtils.getUnmaskedPhone(fieldPhone),
                            fieldPassword.text.toString()))
                }
            }
        }
    }


    private fun isValidFields(): Boolean {


        if (fieldPassword.text.isEmpty()) {
            fieldPassword.requestFocus()
            fieldPassword.error = getString(R.string.fill_field_please)
            return false
        }

        if (fieldPhone.text.isEmpty()) {
            fieldPhone.requestFocus()
            fieldPhone.error = getString(R.string.fill_field_please)
            return false
        }

        return true
    }


    private fun loginByNetworkCall(tokenRequest: UserTokenRequest) {
        RetrofitClient
            .INSTANCE
            .loginUser(tokenRequest)
            .onErrorReturn {
                UserProfileResponse(0)
            }
            .doOnRequest {
                AppExecutorsClient.mainThread().execute {
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                AppExecutorsClient.mainThread().execute {
                    if (it.id == 0) {
                        progressDialog.generalError()
                        AppExecutorsClient.handlerDelayed({
                            progressDialog.dismiss()
                        }, 2000)

                    } else {
                        PreferenceManger.saveToken(it.token)
                        PreferenceManger.saveUserData(UserData(it.phone, it.fullName))
                        AppExecutorsClient.handlerDelayed({
                            val intent = Intent(requireActivity(), HomeActivity::class.java)
                            progressDialog.dismiss()
                            startActivity(intent)
                            requireActivity().finish()
                        }, 1000)
                    }
                }
            }
    }


}