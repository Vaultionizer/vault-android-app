package com.vaultionizer.vaultapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.NavigationDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.descriptionRes
import com.mikepenz.materialdrawer.model.interfaces.descriptionText
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.removeAllItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Config
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Models
    val viewModel: MainActivityViewModel by viewModels()

    // UI
    var itemIdentifier = 0L

    private var addNewSpaceIdentifier: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: MaterialDrawerSliderView = findViewById(R.id.nav_view)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        val userProfile = ProfileDrawerItem().apply {
            nameText = AuthRepository.user?.localUser?.username ?: "Unknown"
            descriptionText = "User ID: ${AuthRepository.user?.localUser?.userId}"
            icon = null
            identifier = nextIdentifier()
        }

        val headerView = AccountHeaderView(this).apply {
            attachToSliderView(navView)
            addProfiles(userProfile)
            dividerBelowHeader = false
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fileFragment, R.id.createSpaceFragment, R.id.keyManagementFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val preserved = navView.onDrawerItemClickListener!!
        navView.onDrawerItemClickListener = { v, drawerItem, position ->
            val copy = drawerItem.tag

            if (copy is VNSpace) {
                actionBar?.title = "Space ${copy.remoteId}"
                viewModel.selectedSpaceChanged(copy)
                Log.e("Vault", "Changing space to ${copy.id}")
            }

            preserved(v, drawerItem, position)
        }

        rebuildGeneralUi(navView)

        viewModel.userSpaces.observe(this, androidx.lifecycle.Observer {
            navView.removeAllItems()
            rebuildGeneralUi(navView)

            navView.apply {
                addItems(NavigationDrawerItem(R.id.createSpaceFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameText = "Add new space"
                    iconicsIcon = FontAwesome.Icon.faw_plus_circle
                    isSelectable = false
                }))
                for (space in it) {
                    addItems(NavigationDrawerItem(R.id.fileFragment,
                        PrimaryDrawerItem().apply {
                            iconicsIcon = if (space.owner) {
                                FontAwesome.Icon.faw_user
                            } else {
                                FontAwesome.Icon.faw_users
                            }

                            identifier = nextIdentifier()
                            nameText = if (space.name != null) {
                                "Space \"${space.name}\""
                            } else {
                                "Space #${space.id}"
                            }
                            isSelectable = false
                        }.apply {
                            identifier = nextIdentifier()
                            tag = space
                            isSelectable = false
                        })
                    )
                }
            }

            if (viewModel.selectedSpace.value == null && it.size > 0) {
                viewModel.selectedSpaceChanged(it[0])
            }
        })

        viewModel.updateUserSpaces()
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

    private fun nextIdentifier(): Long {
        return itemIdentifier++
    }

    private fun rebuildGeneralUi(navigationView: MaterialDrawerSliderView) {
        navigationView.apply {
            addItems(
                SectionDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameText = "App management"
                },
                PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    isSelectable = false
                    nameRes = R.string.menu_settings
                    iconicsIcon = OutlinedGoogleMaterial.Icon.gmo_settings
                    descriptionRes = R.string.menu_settings_description
                },
                NavigationDrawerItem(R.id.keyManagementFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameRes = R.string.menu_keys
                    iconicsIcon = OutlinedGoogleMaterial.Icon.gmo_vpn_key
                    descriptionRes = R.string.menu_keys_description
                }
                ),
                SectionDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameRes = R.string.menu_section_vaults
                }
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val mapFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController.currentDestination?.id
        if (mapFragment == R.id.viewPC && item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}