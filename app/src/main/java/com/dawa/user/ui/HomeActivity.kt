package com.dawa.user.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.dawa.user.R
import com.dawa.user.handlers.PreferenceManagerService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.home_nav_host)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val user = PreferenceManagerService.retrieveUserData()
        FirebaseMessaging.getInstance().subscribeToTopic(user.id.toString())

        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)

        nav_bar.let {
            NavigationUI.setupWithNavController(it, navController)
        }

        fab.setOnClickListener {
            navController.navigate(R.id.addMedicationFragment)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.profileFragment){
            navController.navigate(R.id.preservationsFragment)
        }
        return super.onOptionsItemSelected(item)
    }
}
