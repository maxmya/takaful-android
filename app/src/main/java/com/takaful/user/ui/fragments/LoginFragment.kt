package com.takaful.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.squareup.picasso.Picasso
import com.takaful.user.R
import kotlinx.android.synthetic.main.layout_login.*

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        open_reg_screen.setOnClickListener {
            val toRegistrationAction = LoginFragmentDirections.toRegistration()
            Navigation.findNavController(it).navigate(toRegistrationAction)

        }


    }

}