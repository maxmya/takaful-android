package com.takaful.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import com.takaful.user.App
import com.takaful.user.R
import com.takaful.user.handlers.LogoutHandler
import com.takaful.user.handlers.PreferenceManger
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val navController = Navigation.findNavController(this, R.id.activity_home)
        navController.navigate(R.id.changeProfileFragment, null)


        userFullName.text = PreferenceManger.retrieveUserData().fullName

        logout.setOnClickListener {
            LogoutHandler.doLogout()
            startActivity(Intent(this, SplashActivity::class.java))
        }

    }
}
