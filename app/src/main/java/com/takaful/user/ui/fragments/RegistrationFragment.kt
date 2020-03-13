package com.takaful.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.takaful.user.App
import com.takaful.user.R
import com.takaful.user.network.data.UserRegisterRequest
import com.takaful.user.network.retrofit.RetrofitClient
import com.takaful.user.utils.AppExecutors
import com.takaful.user.utils.StringUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_registration.*


class RegistrationFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val terms = getString(R.string.terms_conditions)
        val word = getString(R.string.terms_conditions_word)

        StringUtils.makeTextColoredAndBold(terms, word, terms_conditions)
        registerAction()
    }


    private fun registerAction() {
        btnCreate.setOnClickListener {

            if (fieldFullName.text.isEmpty()) {
                fieldFullName.requestFocus()
                fieldFullName.error = getString(R.string.fill_field_please)
                return@setOnClickListener
            }

            if (fieldPassword.text.isEmpty()) {
                fieldPassword.requestFocus()
                fieldPassword.error = getString(R.string.fill_field_please)
                return@setOnClickListener
            }

            if (fieldPhone.text.isEmpty()) {
                fieldPhone.requestFocus()
                fieldPhone.error = getString(R.string.fill_field_please)
                return@setOnClickListener
            }


            val accountRequest =
                UserRegisterRequest(
                    fieldPhone.text.toString(),
                    fieldPassword.text.toString(),
                    fieldPhone.text.toString(),
                    fieldFullName.text.toString(),
                    "no-url"
                )

            // network call with retrofit and choosing thread
            // and scheduler for doing network call
            RetrofitClient
                .INSTANCE
                .registerUser(accountRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .doOnError {
                    App.appExecutors.mainThread().execute {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
                }
                .subscribe {
                    App.appExecutors.mainThread().execute {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
                }


        }
    }


}