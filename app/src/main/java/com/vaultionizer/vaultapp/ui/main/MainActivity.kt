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
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.removeAllItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.ui.common.dialog.AlertDialogType
import com.vaultionizer.vaultapp.ui.main.file.FileEvent
import com.vaultionizer.vaultapp.ui.viewmodel.FileStatusViewModel
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Config
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Models
    @Inject
    lateinit var authCache: AuthCache
    val viewModel: MainActivityViewModel by viewModels()
    val fileStatusViewModel: FileStatusViewModel by viewModels()

    // UI
    var itemIdentifier = 0L

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
            nameText = authCache.loggedInUser?.localUser?.username ?: "Unknown"
            descriptionText = "User ID: ${authCache.loggedInUser?.localUser?.userId}"
            icon = null
            identifier = nextIdentifier()
        }

        AccountHeaderView(this).apply {
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
                trySwitchSpace(copy)
            }

            preserved(v, drawerItem, position)
        }

        rebuildGeneralUi(navView)
        viewModel.userSpaces.observe(this) {
            navView.removeAllItems()
            rebuildGeneralUi(navView)

            navView.apply {
                for (space in it) {
                    addItems(buildSpaceDrawerItem(space))
                }
            }
        }

        viewModel.fileEvent.observe(this) {
            if (it is FileEvent.EncryptionKeyRequired) {
                val generateKeyCallback: (dialog: MaterialDialog) -> Unit = { dialog ->
                    dialog.dismiss()
                    viewModel.generateSpaceKey(it.space)
                    trySwitchSpace(it.space)
                }

                val importKeyCallback: (dialog: MaterialDialog) -> Unit = { _ ->
                    // TODO(jatsqi): Import key
                }

                val dialog =
                    AlertDialogType.REQUEST_KEY_GENERATION.createDialog(
                        this,
                        positiveClick = generateKeyCallback,
                        negativeClick = importKeyCallback
                    )
                dialog.cancelable(false)
                dialog.show()
            }
        }

        fileStatusViewModel.workInfo.observe(this) {
            fileStatusViewModel.onWorkerStatusChange(it)
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val mapFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController.currentDestination?.id
        if (mapFragment == R.id.viewPC && item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun trySwitchSpace(space: VNSpace) {
        if (viewModel.selectedSpaceChanged(space)) {
            actionBar?.title = "Space ${space.remoteId}"
            Log.e("Vault", "Changing space to ${space.id}")
        }
    }

    private fun nextIdentifier(): Long {
        return itemIdentifier++
    }

    private fun rebuildGeneralUi(navigationView: MaterialDrawerSliderView) {
        rebuildGeneralManagementUi(navigationView)
        rebuildGeneralSpaceUi(navigationView)
    }

    private fun rebuildGeneralSpaceUi(navigationView: MaterialDrawerSliderView) {
        navigationView.apply {
            addItems(
                NavigationDrawerItem(R.id.createSpaceFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameText = getString(R.string.create_space_navigation)
                    iconicsIcon = FontAwesome.Icon.faw_plus_circle
                    isSelectable = false
                }),
                NavigationDrawerItem(R.id.joinSpaceFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameText = getString(R.string.join_space_navigation)
                    iconDrawable = getDrawable(R.drawable.ic_baseline_qr_code_scanner_24)
                    isSelectable = false
                })
            )
        }
    }

    private fun rebuildGeneralManagementUi(navigationView: MaterialDrawerSliderView) {
        navigationView.apply {
            addItems(
                SectionDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameText = "App management"
                },
                NavigationDrawerItem(R.id.settingsFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameRes = R.string.menu_settings
                    iconicsIcon = OutlinedGoogleMaterial.Icon.gmo_settings
                    descriptionRes = R.string.menu_settings_description
                }),
                NavigationDrawerItem(R.id.keyManagementFragment, PrimaryDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameRes = R.string.menu_keys
                    iconicsIcon = OutlinedGoogleMaterial.Icon.gmo_vpn_key
                    descriptionRes = R.string.menu_keys_description
                }),
                SectionDrawerItem().apply {
                    identifier = nextIdentifier()
                    nameRes = R.string.menu_section_vaults
                }
            )
        }
    }

    private fun buildSpaceDrawerItem(space: VNSpace): NavigationDrawerItem<AbstractBadgeableDrawerItem.ViewHolder> {
        val primary = PrimaryDrawerItem().apply {
            iconicsIcon = if (space.isPrivate) {
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
        }

        return NavigationDrawerItem(R.id.fileFragment, primary)
    }
}