package com.takaful.user.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.takaful.user.App
import com.takaful.user.R
import com.takaful.user.handlers.AppExecutorsClient
import com.takaful.user.handlers.PreferenceManger

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
