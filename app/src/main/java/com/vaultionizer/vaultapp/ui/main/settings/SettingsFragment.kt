package com.vaultionizer.vaultapp.ui.main.settings

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.common.dialog.AlertDialogType
import com.vaultionizer.vaultapp.ui.common.dialog.showDialog
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsAction
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsActionEnum
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logoutBtn: Preference? = findPreference("logoutBtn")
        val deleteLocalFilesBtn: Preference? = findPreference("deleteLocalFilesBtn")
        val deleteUserBtn: Preference? = findPreference("deleteUserBtn")
        val quitAllSpacesBtn: Preference? = findPreference("quitAllSpacesBtn")

        setupBtn(logoutBtn, null) { viewModel.logout() }
        setupBtn(
            deleteLocalFilesBtn,
            AlertDialogType.DELETE_LOCAL_FILES
        ) { viewModel.deleteLocalFiles() }
        setupBtn(deleteUserBtn, AlertDialogType.DELETE_USER) { viewModel.deleteUser() }
        setupBtn(
            quitAllSpacesBtn,
            AlertDialogType.QUIT_ALL_SPACES
        ) { viewModel.quitAllSpaces() }

        val themePreference = findPreference<ListPreference>("theme_list")
        themePreference?.setValueIndex(0)
        viewModel.settingsActionResult.observe(viewLifecycleOwner) {
            actionStatusChanged(it)
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

    private fun actionStatusChanged(status: SettingsAction) {
        if (!status.done) {
            progressDialog = ProgressDialog.show(
                context, "",
                "Loading. Please wait...", true
            )
            return
        }
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE") var goToLogin = false
        progressDialog?.dismiss()
        var toastTextId: Int?
        toastTextId = R.string.toast_success_quit_spaces
        if (status.success) {
            when (status.action) {
                SettingsActionEnum.LOGOUT -> {
                    toastTextId = R.string.toast_success_logout
                    goToLogin = true
                }
                SettingsActionEnum.DELETE_LOCAL_FILES -> {
                    toastTextId = R.string.toast_success_quit_spaces

                }
                SettingsActionEnum.DELETE_USER -> {
                    toastTextId = R.string.toast_success_delete_user
                    goToLogin = true
                }
                SettingsActionEnum.QUIT_ALL_SPACES -> {
                    toastTextId = R.string.toast_success_delete_local_files
                }
            }
        } else {
            when (status.action) {
                SettingsActionEnum.LOGOUT -> {
                    toastTextId = R.string.toast_failed_logout
                }
                SettingsActionEnum.DELETE_LOCAL_FILES -> {
                    toastTextId = R.string.toast_failed_quit_spaces

                }
                SettingsActionEnum.DELETE_USER -> {
                    toastTextId = R.string.toast_failed_delete_user

                }
                SettingsActionEnum.QUIT_ALL_SPACES -> {
                    toastTextId = R.string.toast_failed_delete_local_files

                }
            }
        }
        //if (goToLogin) navigateLogin()
        Toast.makeText(context, toastTextId, Toast.LENGTH_LONG).show()
        // TODO: show toast (does not work yet)
    }

    private fun navigateLogin() {
        // TODO(keksklauer4): Navigate to LoginFragment
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAuthenticationActivity())
    }

}