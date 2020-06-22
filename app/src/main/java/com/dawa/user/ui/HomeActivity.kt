package com.dawa.user.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.dawa.user.R
import com.dawa.user.handlers.PreferenceManagerService
import com.google.firebase.messaging.FirebaseMessaging
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceNavigationView
import com.luseen.spacenavigation.SpaceOnClickListener
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

        val spaceNavigationView = findViewById<View>(R.id.nav_bar) as SpaceNavigationView

        spaceNavigationView.addSpaceItem(SpaceItem(null, R.drawable.account_off))
        spaceNavigationView.addSpaceItem(SpaceItem(null, R.drawable.notification_off))
        spaceNavigationView.addSpaceItem(SpaceItem(null, R.drawable.drug))
        spaceNavigationView.addSpaceItem(SpaceItem(null, R.drawable.home_active))

        spaceNavigationView.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                navController.navigate(R.id.addMedicationFragment)
            }

            override fun onItemReselected(itemIndex: Int, itemName: String?) {
                selectWithNavbar(itemIndex)
            }

            override fun onItemClick(itemIndex: Int, itemName: String?) {
                selectWithNavbar(itemIndex)
            }

        })

    }

    private fun selectWithNavbar(itemIndex: Int) {
        when (itemIndex) {
            0 -> {
                navController.navigate(R.id.profileFragment)
            }
            1 -> {
                navController.navigate(R.id.myNotificationListFragment)
            }
            2 -> {

            }
            3 -> {
                navController.navigate(R.id.homeFragment)
            }
        }
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
