package com.takaful.user.ui.fragments

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
import com.takaful.user.exceptions.ConnectionException
import com.takaful.user.network.data.UserRegisterRequest
import com.takaful.user.network.data.UserRegisterResponse
import com.takaful.user.network.data.UserTokenRequest
import com.takaful.user.network.retrofit.RetrofitClient
import com.takaful.user.ui.MessageProgressDialog
import com.takaful.user.utils.StringUtils
import com.takaful.user.utils.StringUtils.Companion.getUnmaskedPhone
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_registration.*
import kotlinx.android.synthetic.main.layout_registration.fieldPassword
import kotlinx.android.synthetic.main.layout_registration.fieldPhone
import java.util.regex.Pattern


class RegistrationFragment : Fragment() {

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
        return inflater.inflate(R.layout.layout_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StringUtils.maskPhoneField(fieldPhone)

        back_to_login.setOnClickListener {
            openLogin(it, null)
        }

        val terms = getString(R.string.terms_conditions)
        val word = getString(R.string.terms_conditions_word)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val toTermsAction = RegistrationFragmentDirections.toTerms()
                Navigation.findNavController(textView).navigate(toTermsAction)
            }
        }

        StringUtils.makeUrlColoredSpan(terms, word, terms_conditions, clickableSpan)
        registerAction()


        progressDialog = MessageProgressDialog(requireActivity())


    }

    private fun isValidFields(): Boolean {

        if (fieldFullName.text.isEmpty()) {
            fieldFullName.requestFocus()
            fieldFullName.error = getString(R.string.fill_field_please)
            return false
        } else {
            val isValidFullName = Pattern
                .compile(getString(R.string.full_name_regex))
                .matcher(fieldFullName.text.toString())
                .matches()
            if (!isValidFullName || fieldFullName.text.length < 5) {
                fieldFullName.requestFocus()
                fieldFullName.error = getString(R.string.valid_field_please)
                return false
            }
        }

        if (fieldPassword.text.isEmpty()) {
            fieldPassword.requestFocus()
            fieldPassword.error = getString(R.string.fill_field_please)
            return false
        } else if (fieldPassword.text.length < 6) {
            fieldPassword.requestFocus()
            fieldPassword.error = getString(R.string.valid_password_length)
            return false
        }


        if (fieldPhone.text.isEmpty()) {
            fieldPhone.requestFocus()
            fieldPhone.error = getString(R.string.fill_field_please)
            return false
        } else {
            val isValidPhone = Pattern
                .compile(getString(R.string.phone_number_regex))
                .matcher(getUnmaskedPhone(fieldPhone))
                .matches()

            if (!isValidPhone) {
                fieldPhone.requestFocus()
                fieldPhone.error = getString(R.string.valid_field_please)
                return false
            }
        }

        return true
    }


    private fun registerAction() {

        btnCreate.setOnClickListener {

            if (!isValidFields())
                return@setOnClickListener

            val accountRequest =
                UserRegisterRequest(
                    getUnmaskedPhone(fieldPhone),
                    fieldPassword.text.toString().trim(),
                    getUnmaskedPhone(fieldPhone),
                    fieldFullName.text.toString().trim(),
                    "no-url"
                )

            makeRegisterWithNetworkCall(accountRequest)

        }
    }


    private fun makeRegisterWithNetworkCall(accountRequest: UserRegisterRequest) {

        RetrofitClient
            .INSTANCE
            .registerUser(accountRequest)
            .onErrorReturn {
                UserRegisterResponse(false, getString(R.string.general_error))
            }
            .doOnRequest {
                App.appExecutors.mainThread().execute {
                    btnCreate.isEnabled = false
                    progressDialog.loading()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                App.appExecutors.mainThread().execute {
                    progressDialog.show(it.message)
                    if (it.success) {
                        Handler().postDelayed({
                            progressDialog.dismiss()
                            openLogin(
                                btnCreate,
                                UserTokenRequest(
                                    getUnmaskedPhone(fieldPhone),
                                    fieldPassword.text.toString()
                                )
                            )
                        }, 1000)
                    } else {
                        btnCreate.isEnabled = true
                        Handler().postDelayed({
                            progressDialog.dismiss()
                        }, 3000)
                    }
                }
            }


    }

    private fun openLogin(view: View, userLoginData: UserTokenRequest?) {
        val toLogin = RegistrationFragmentDirections.toLogin(userLoginData)
        Navigation.findNavController(view).navigate(toLogin)

    }

}