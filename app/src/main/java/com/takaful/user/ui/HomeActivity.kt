package com.takaful.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.takaful.user.App
import com.takaful.user.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userFullName.text = App.userFullName

        logout.setOnClickListener {
            App.TOKEN = ""
            App.userFullName = ""
            App.sPrefs.edit().putString(App.TOKEN_KEY, "").apply()
            App.sPrefs.edit().putString(App.USER_FULL_NAME, "").apply()
            startActivity(Intent(this, SplashActivity::class.java))
        }

    }
}
