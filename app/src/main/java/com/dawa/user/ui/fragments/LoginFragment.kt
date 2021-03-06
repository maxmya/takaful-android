package com.dawa.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.data.UserData
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService
import com.dawa.user.network.data.*
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.HomeActivity
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.utils.StringUtils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_login.fieldPassword
import kotlinx.android.synthetic.main.layout_login.fieldPhone
import kotlinx.android.synthetic.main.layout_registration.*


class LoginFragment : Fragment() {

    private var callbackManager: CallbackManager? = null
    private var firebaseAuth: FirebaseAuth? = null
    lateinit var progressDialog: MessageProgressDialog


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_login, container, false)
    }


    private fun setupUi() {
        StringUtils.maskPhoneField(fieldPhone)
        progressDialog = MessageProgressDialog(requireActivity())
        val statement = getString(R.string.register_a_new_account)
        val word = getString(R.string.create_account)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val toRegistrationAction = LoginFragmentDirections.toRegistration()
                Navigation.findNavController(textView).navigate(toRegistrationAction)
            }
        }

        StringUtils.makeUrlColoredSpan(statement, word, open_reg_screen, clickableSpan)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()

        if (firebaseAuth?.currentUser != null) {
            userAlreadyLoggedIn()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        requireArguments().let {
            val loginRequest = LoginFragmentArgs.fromBundle(it).userRequest
            if (loginRequest != null) {
                fieldPhone.setText(loginRequest.username)
                fieldPassword.setText(loginRequest.password)
                AppExecutorsService.handlerDelayed({
                    progressDialog.loading()
                    loginByNetworkCall(loginRequest)
                }, 1000)
            } else {
                btnLogin.setOnClickListener {
                    if (!isValidFields()) return@setOnClickListener
                    loginByNetworkCall(UserTokenRequest(StringUtils.getUnmaskedPhone(fieldPhone),
                            fieldPassword.text.toString()))
                }
            }
        }

        forgetPassword.setOnClickListener {
            val toForgetPassword = LoginFragmentDirections.toForgetPassword()
            Navigation.findNavController(forgetPassword).navigate(toForgetPassword)
        }

    }

    private fun userAlreadyLoggedIn() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
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
        RetrofitClient.INSTANCE.loginUser(tokenRequest).onErrorReturn {
            if (it.message != null) {
                ResponseWrapper(false, it.message!!, null)
            } else {
                ResponseWrapper(false, "error occurred", null)
            }
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                val userData: UserProfileResponse? = it.data
                if (!it.success || userData == null) {
                    progressDialog.generalError()
                    AppExecutorsService.handlerDelayed({
                        progressDialog.dismiss()
                    }, 2000)
                } else {
                    PreferenceManagerService.saveToken(userData.token)
                    PreferenceManagerService.saveUserData(UserData(userData.id,
                            userData.phone,
                            userData.fullName,
                            userData.pictureUrl))
                    AppExecutorsService.handlerDelayed({
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        progressDialog.dismiss()
                        startActivity(intent)
                        requireActivity().finish()
                    }, 1000)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

    }

}