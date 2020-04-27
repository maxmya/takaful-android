package com.dawa.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dawa.user.R
import com.dawa.user.handlers.AppExecutorsClient
import com.dawa.user.handlers.PreferenceManger

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppExecutorsClient.handlerDelayed({
                openDesiredActivityPath()
            }, 3000
        )
    }

    private fun openDesiredActivityPath() {
        if (PreferenceManger.retrieveToken().isEmpty()) {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
