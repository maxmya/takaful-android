package com.takaful.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.takaful.user.App
import com.takaful.user.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            openDesiredActivityPath()
        }, 3000
        )
    }

    private fun openDesiredActivityPath() {
        if (App.TOKEN.isEmpty()) {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
