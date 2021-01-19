package com.dawa.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppExecutorsService.handlerDelayed({
            openDesiredActivityPath()
        }, 3000)
    }

    private fun openDesiredActivityPath() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
