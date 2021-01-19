package com.dawa.user.ui.fragments

import android.os.Bundle
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dawa.user.R
import com.dawa.user.network.data.UserRegisterRequest
import com.dawa.user.network.data.UserRegisterResponse
import com.dawa.user.network.data.UserTokenRequest
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.ui.dialogs.AuthCodeDialog
import com.dawa.user.utils.StringUtils
import com.dawa.user.utils.StringUtils.getUnmaskedPhone
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_registration.*
import kotlinx.android.synthetic.main.layout_registration.fieldPassword
import kotlinx.android.synthetic.main.layout_registration.fieldPhone
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private const val TAG = "RegistrationFragment"

class RegistrationFragment : Fragment() {


    lateinit var progressDialog: MessageProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StringUtils.maskPhoneField(fieldPhone)


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
            val isValidFullName = Pattern.compile(getString(R.string.full_name_regex))
                .matcher(fieldFullName.text.toString()).matches()
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
            fieldPhone.error = getString(R.string.valid_field_please)
            return false
        }

        return true
    }

    private var isRegistering = false
    private var manualScenario = false


    private fun registerAction() {

        btnCreate.setOnClickListener {

            isRegistering = false
            manualScenario = false

            if (!isValidFields()) return@setOnClickListener

            val phoneForFirebase = getUnmaskedPhone(fieldPhone)
            Log.d(TAG, "registerAction: phone is $phoneForFirebase")

            PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneForFirebase,
                    60,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    verificationCallback)
            progressDialog.show("جاري التحقق من رقم الهاتف")
        }
    }

    private val verificationCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: verified from itself")
                if (!manualScenario) callMakeRequestWithBuilder()
            }

            override fun onVerificationFailed(exp: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ", exp)
            }

            override fun onCodeSent(verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                AppExecutorsService.handlerDelayed({
                    if (!isRegistering) {
                        manualScenario = true
                        val authCodeFromDialog = object : AuthCodeFromDialog {
                            override fun authCode(code: String) {
                                val cred = PhoneAuthProvider.getCredential(verificationId, code)
                                FirebaseAuth.getInstance().signInWithCredential(cred)
                                    .addOnCompleteListener(requireActivity()) { task ->
                                        if (task.isSuccessful) {
                                            callMakeRequestWithBuilder()
                                        } else {
                                            progressDialog.show("حدث خطأ ما , لم يتم تأكيد رقم الهاتف")
                                            AppExecutorsService.handlerDelayed({ progressDialog.dismiss() },
                                                    2000)
                                        }
                                    }
                            }
                        }

                        AuthCodeDialog(requireActivity(), authCodeFromDialog)


                    }
                }, 2000)
                Log.d(TAG, "onCodeSent: code sent and it's $verificationId and token is ${token}")
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                progressDialog.show("حدث خطأ ما , لم يتم تأكيد رقم الهاتف")
                AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 2000)
            }
        }


    public interface AuthCodeFromDialog {
        fun authCode(code: String)
    }

    private fun callMakeRequestWithBuilder() {
        val accountRequest = UserRegisterRequest(getUnmaskedPhone(fieldPhone),
                fieldPassword.text.toString().trim(),
                getUnmaskedPhone(fieldPhone),
                fieldFullName.text.toString().trim(),
                "no-url")
        makeRegisterWithNetworkCall(accountRequest)
    }

    private fun makeRegisterWithNetworkCall(accountRequest: UserRegisterRequest) {
        isRegistering = true;
        RetrofitClient.INSTANCE.registerUser(accountRequest).onErrorReturn {
            UserRegisterResponse(false, getString(R.string.general_error))
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                btnCreate.isEnabled = false
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                progressDialog.show(it.message)
                if (it.success) {
                    AppExecutorsService.handlerDelayed({
                        progressDialog.dismiss()
                        openLogin(btnCreate,
                                UserTokenRequest(getUnmaskedPhone(fieldPhone),
                                        fieldPassword.text.toString()))
                    }, 1000)
                } else {
                    btnCreate.isEnabled = true
                    AppExecutorsService.handlerDelayed({
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