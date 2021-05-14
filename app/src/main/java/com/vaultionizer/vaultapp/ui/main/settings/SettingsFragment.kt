package com.vaultionizer.vaultapp.ui.main.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.main.file.FileAlertDialogType
import com.vaultionizer.vaultapp.ui.main.file.showDialog
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsAction
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsActionEnum
import com.vaultionizer.vaultapp.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

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
            FileAlertDialogType.DELETE_LOCAL_FILES
        ) { viewModel.deleteLocalFiles() }
        setupBtn(deleteUserBtn, FileAlertDialogType.DELETE_USER) { viewModel.deleteUser() }
        setupBtn(
            quitAllSpacesBtn,
            FileAlertDialogType.QUIT_ALL_SPACES
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
        dialogType: FileAlertDialogType?,
        posCallback: () -> Unit
    ) {
        preference?.setOnPreferenceClickListener { _ ->
            if (dialogType != null) showDialog(dialogType, positiveClick = { posCallback() })
            else posCallback()
            return@setOnPreferenceClickListener true
        }
    }

    private fun actionStatusChanged(status: SettingsAction) {
        var toastId: Int?
        if (status.success) {
            when (status.action) {
                SettingsActionEnum.LOGOUT -> {
                    toastId = R.string.toast_success_logout

                }
                SettingsActionEnum.DELETE_LOCAL_FILES -> {
                    toastId = R.string.toast_success_quit_spaces

                }
                SettingsActionEnum.DELETE_USER -> {
                    toastId = R.string.toast_success_delete_user

                }
                SettingsActionEnum.QUIT_ALL_SPACES -> {
                    toastId = R.string.toast_success_delete_local_files
                }
            }
        } else {
            when (status.action) {
                SettingsActionEnum.LOGOUT -> {
                    toastId = R.string.toast_failed_logout
                }
                SettingsActionEnum.DELETE_LOCAL_FILES -> {
                    toastId = R.string.toast_failed_quit_spaces

                }
                SettingsActionEnum.DELETE_USER -> {
                    toastId = R.string.toast_failed_delete_user

                }
                SettingsActionEnum.QUIT_ALL_SPACES -> {
                    toastId = R.string.toast_failed_delete_local_files

                }
            }
        }
        Toast.makeText(context, toastId, Toast.LENGTH_SHORT).show()
    }

}