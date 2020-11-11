package com.vaultionizer.vaultapp.ui.main

import android.os.Bundle
import android.view.Menu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.vaultionizer.vaultapp.R
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: MaterialDrawerSliderView = findViewById(R.id.nav_view)
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        val userProfile = ProfileDrawerItem().apply {
            nameText = "Max Muster"
            descriptionText = UUID.randomUUID().toString()
            icon = null
            identifier = 100
        }

        val headerView = AccountHeaderView(this).apply {
            attachToSliderView(navView)
            addProfiles(userProfile)
            dividerBelowHeader = false
        }

        navView.apply {
            addItems(
                    SectionDrawerItem().apply {
                        nameText = "App management"
                    },
                    PrimaryDrawerItem().apply {
                        nameRes = R.string.menu_settings
                        iconicsIcon = GoogleMaterial.Icon.gmd_settings
                        descriptionRes = R.string.menu_settings_description
                    },
                    PrimaryDrawerItem().apply {
                        nameRes = R.string.menu_keys
                        iconicsIcon = GoogleMaterial.Icon.gmd_vpn_key
                        descriptionRes = R.string.menu_keys_description
                    },
                    SectionDrawerItem().apply {
                        nameRes = R.string.menu_section_vaults
                    },
                    PrimaryDrawerItem().apply {
                        iconicsIcon = FontAwesome.Icon.faw_lock
                        nameText = "Personal"
                    }
            )
        }

        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.fileFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}