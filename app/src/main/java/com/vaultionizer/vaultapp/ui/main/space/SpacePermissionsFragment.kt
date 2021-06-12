package com.vaultionizer.vaultapp.ui.main.space

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.common.dialog.AlertDialogType
import com.vaultionizer.vaultapp.ui.common.dialog.showDialog
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import com.vaultionizer.vaultapp.ui.viewmodel.ManageSpaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SpacePermissionsFragment : PreferenceFragmentCompat() {
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: ManageSpaceViewModel by viewModels()

    private var owner = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        owner = mainActivityViewModel.currentDirectory.value!!.space.owner
        setupViewModel()
        if (owner) setPreferencesFromResource(R.xml.space_management_creator, rootKey)
        else setPreferencesFromResource(R.xml.space_management_user, rootKey)
    }

    fun setupViewModel() {
        viewModel.spaceID = mainActivityViewModel.selectedSpace.value?.id ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val showAuthKeyBtn: Preference? = findPreference("showAuthKeyBtn")

        setupBtn(showAuthKeyBtn, null) { showAuthKey() }

        val writeAccessSwitch: SwitchPreference? = findPreference("writeAccessSwitch")
        if (writeAccessSwitch != null) writeAccessSwitch.isChecked = true // TODO

        if (!owner) {
            val quitSpaceBtn: Preference? = findPreference("quitSpaceBtn")
            setupBtn(
                quitSpaceBtn,
                AlertDialogType.QUIT_SPACE
            ) { mainActivityViewModel.requestQuitSpace() }
        } else {
            val sharedSpaceSwitch: SwitchPreference? = findPreference("sharedSpaceSwitch")
            val usersInviteAuthSwitch: SwitchPreference? = findPreference("authKeySwitch")
            val deleteSpaceBtn: Preference? = findPreference("deleteSpaceBtn")
            val genAuthKeyBtn: Preference? = findPreference("generateAuthKeyBtn")
            val kickUsersBtn: Preference? = findPreference("kickUsersBtn")

            // setup button listeners
            setupBtn(kickUsersBtn, AlertDialogType.KICK_ALL_USERS) { viewModel.kickAllUsers() }
            setupBtn(
                deleteSpaceBtn,
                AlertDialogType.DELETE_SPACE
            ) { mainActivityViewModel.requestSpaceDeletion() }
            setupBtn(
                genAuthKeyBtn,
                AlertDialogType.REGENERATE_AUTH_KEY
            ) { viewModel.generateAuthKey() }

            // setup switch listeners
            setupSwitch(writeAccessSwitch, null,
                { viewModel.changeWriteAccess(writeAccessSwitch?.isChecked) },
                { _ -> return@setupSwitch false })
            setupSwitch(
                sharedSpaceSwitch,
                AlertDialogType.MAKE_SPACE_PRIVATE,
                { viewModel.toggleSharedSpace(sharedSpaceSwitch?.isChecked) },
                { switch -> return@setupSwitch switch.isChecked })
            setupSwitch(usersInviteAuthSwitch, null,
                { viewModel.toggleUsersInvite(usersInviteAuthSwitch?.isChecked) },
                { _ -> return@setupSwitch false })

            viewModel.spaceConfig.observe(viewLifecycleOwner) { viewModel.configureSpace() }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupBtn(
        preference: Preference?,
        dialogType: AlertDialogType?,
        posCallback: () -> Unit
    ) {
        preference?.setOnPreferenceClickListener { _ ->
            if (dialogType != null) showDialog(dialogType, positiveClick = { posCallback() })
            else posCallback()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setupSwitch(
        switchPref: SwitchPreference?,
        dialogType: AlertDialogType?,
        posCallback: () -> Unit,
        extraCond: (SwitchPreference) -> Boolean
    ) {
        switchPref?.setOnPreferenceChangeListener { _, _ ->
            if (dialogType != null && extraCond(switchPref)) {
                showDialog(dialogType, positiveClick = { posCallback() }, {
                    switchPref.isChecked = !switchPref.isChecked
                })
            } else posCallback()

            switchPref.isChecked = true
            return@setOnPreferenceChangeListener true
        }
    }


    private fun showAuthKey() {
        findNavController().navigate(
            SpacePermissionsFragmentDirections.actionSpacePermissionsFragmentToAuthKeyFragment(
                "Test", "symmetric", 69
            )
        )
    }


}
