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
        /* facebook_sign_out_button.setOnClickListener {
             signOut()
         }*/
        //check if user already logged in and token not expired
        if (firebaseAuth?.currentUser != null) {
            userAlreadyLoggedIn()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        facebook_sign_in_button.setPermissions("email")
        facebook_sign_in_button.fragment = this

        facebook_sign_in_button.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        handleFacebookAccessToken(loginResult.accessToken);

                    }

                    override fun onCancel() {

                    }

                    override fun onError(exception: FacebookException) {
                    }
                })
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

        textView3.setOnClickListener {
            val intent = Intent(requireActivity(), HomeActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
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

    private fun handleFacebookAccessToken(token: AccessToken) {
        println("faceBook token :$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("signInWithCredential:success")
                    val user = firebaseAuth!!.currentUser
                    var phoneNumber = ""
                    if (user != null) {
//                        updateUI(user)
                        if (user.phoneNumber != null) {
                            phoneNumber = user.phoneNumber!!
                        } else {
                            phoneNumber = user.email.toString()
                        }
                        val userRequestBody = UserTokenRequest(phoneNumber, user.uid)

                        if (user.uid != firebaseAuth!!.uid) {
                            val accountRequest = UserRegisterRequest(phoneNumber,
                                    user.uid,
                                    phoneNumber,
                                    user.displayName.toString(),
                                    user.photoUrl.toString())
                            makeRegisterWithNetworkCall(accountRequest)
                        } else {
                            loginByNetworkCall(userRequestBody)
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    println("signInWithCredential:failure")
                    Toast.makeText(this.context, "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
//                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

    }

    private fun makeRegisterWithNetworkCall(accountRequest: UserRegisterRequest) {
        val userRequestBody = UserTokenRequest(accountRequest.username, accountRequest.password)
        RetrofitClient.INSTANCE.registerUser(accountRequest).onErrorReturn {
                UserRegisterResponse(false, getString(R.string.general_error))
            }.doOnRequest {
                AppExecutorsService.mainThread().execute {
                    progressDialog.loading()
                }
            }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
                AppExecutorsService.mainThread().execute {
                    progressDialog.show(it.message)
                    if (it.success) {
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                            loginByNetworkCall(userRequestBody)
                        }, 1000)
                    } else {
                        AppExecutorsService.handlerDelayed({
                            progressDialog.dismiss()
                        }, 3000)
                    }
                }
            }


    }

    private fun signOut() {
        firebaseAuth?.signOut()
        LoginManager.getInstance().logOut()
//        updateUI(null)
    }

    /*private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            facebook_sign_in_button.visibility = View.GONE
            facebook_sign_out_button.visibility = View.VISIBLE
        } else {
            facebook_sign_in_button.visibility = View.VISIBLE
            facebook_sign_out_button.visibility = View.GONE
        }
    }*/
}