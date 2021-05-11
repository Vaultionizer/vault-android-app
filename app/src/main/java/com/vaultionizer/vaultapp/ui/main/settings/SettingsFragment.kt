package com.vaultionizer.vaultapp.ui.main.settings

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.main.file.FileAlertDialogType
import com.vaultionizer.vaultapp.ui.main.file.showDialog

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logoutBtn: Preference? = findPreference("logoutBtn")
        val deleteLocalFilesBtn: Preference? = findPreference("deleteLocalFilesBtn")
        val deleteUserBtn: Preference? = findPreference("deleteUserBtn")
        val quitAllSpacesBtn: Preference? = findPreference("quitAllSpacesBtn")

        setupBtn(logoutBtn, null) { logout() }
        setupBtn(deleteLocalFilesBtn, FileAlertDialogType.DELETE_LOCAL_FILES) { deleteLocalFiles() }
        setupBtn(deleteUserBtn, FileAlertDialogType.DELETE_USER) { deleteUser() }
        setupBtn(quitAllSpacesBtn, FileAlertDialogType.QUIT_ALL_SPACES) { quitAllSpaces() }

        val themePreference = findPreference<ListPreference>("theme_list")
        themePreference?.setValueIndex(0)
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

    private fun logout(){

    }

    private fun deleteUser(){

    }

    private fun deleteLocalFiles(){

    }

    private fun quitAllSpaces(){

    }
}