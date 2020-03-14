package com.takaful.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.github.vacxe.phonemask.PhoneMaskManager
import com.takaful.user.App
import com.takaful.user.R
import com.takaful.user.network.data.UserProfileResponse
import com.takaful.user.network.data.UserTokenRequest
import com.takaful.user.network.retrofit.RetrofitClient
import com.takaful.user.ui.HomeActivity
import com.takaful.user.ui.MessageProgressDialog
import com.takaful.user.utils.StringUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_login.fieldPassword
import kotlinx.android.synthetic.main.layout_login.fieldPhone
import kotlinx.android.synthetic.main.layout_registration.*

class LoginFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        progressDialog = MessageProgressDialog(requireActivity())

        requireArguments().let {
            val loginRequest = LoginFragmentArgs.fromBundle(it).userRequest

            if (loginRequest != null) {
                fieldPhone.setText(loginRequest.username)
                fieldPassword.setText(loginRequest.password)
                Handler().postDelayed({
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
                            fieldPassword.text.toString()
                        )
                    )
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
                App.appExecutors.mainThread().execute {
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                App.appExecutors.mainThread().execute {
                    if (it.id == 0) {
                        progressDialog.generalError()
                        Handler().postDelayed({
                            progressDialog.dismiss()
                        }, 2000)
                    } else {
                        App.TOKEN = it.token
                        App.userFullName = it.fullName
                        App.sPrefs.edit().putString(App.TOKEN_KEY, App.TOKEN).apply()
                        App.sPrefs.edit().putString(App.USER_FULL_NAME, App.userFullName).apply()
                        Handler().postDelayed({
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