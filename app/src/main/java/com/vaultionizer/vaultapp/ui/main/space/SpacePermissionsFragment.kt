package com.vaultionizer.vaultapp.ui.main.space

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.main.file.FileAlertDialogType
import com.vaultionizer.vaultapp.ui.main.file.showDialog
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SpacePermissionsFragment : PreferenceFragmentCompat(){
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private var owner = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        owner = true // mainActivityViewModel.currentDirectory.value!!.space.owner
        if (owner) setPreferencesFromResource(R.xml.space_management_creator, rootKey)
        else setPreferencesFromResource(R.xml.space_management_user, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val showAuthKeyBtn: Preference? = findPreference("showAuthKeyBtn")

        setupBtn(showAuthKeyBtn, null) { showAuthKey() }

        val writeAccessSwitch : SwitchPreference? = findPreference("writeAccessSwitch")
        if (writeAccessSwitch != null) writeAccessSwitch.isChecked = true // TODO

        if (!owner){
            val quitSpaceBtn : Preference? = findPreference("quitSpaceBtn")
            setupBtn(quitSpaceBtn, FileAlertDialogType.QUIT_SPACE) { quitSpace() }
        }
        else {
            val sharedSpaceSwitch : SwitchPreference? = findPreference("sharedSpaceSwitch")
            val usersInviteAuthSwitch : SwitchPreference? = findPreference("authKeySwitch")
            val deleteSpaceBtn : Preference? = findPreference("deleteSpaceBtn")
            val genAuthKeyBtn : Preference? = findPreference("generateAuthKeyBtn")
            val kickUsersBtn : Preference? = findPreference("kickUsersBtn")

            // setup button listeners
            setupBtn(kickUsersBtn, FileAlertDialogType.KICK_ALL_USERS) { kickAllUsers() }
            setupBtn(deleteSpaceBtn, FileAlertDialogType.DELETE_SPACE) { deleteSpace() }
            setupBtn(genAuthKeyBtn, FileAlertDialogType.REGENERATE_AUTH_KEY) { generateAuthKey() }

            // setup switch listeners
            setupSwitch(writeAccessSwitch, null,
                { changeWriteAccess(writeAccessSwitch?.isChecked) })
            setupSwitch(sharedSpaceSwitch, FileAlertDialogType.MAKE_SPACE_PRIVATE,
                {toggleSharedSpace(sharedSpaceSwitch?.isChecked)}, sharedSpaceSwitch?.isChecked)
            setupSwitch(usersInviteAuthSwitch, null,
                { toggleUsersInvite(usersInviteAuthSwitch?.isChecked) })
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupBtn(preference: Preference?, dialogType: FileAlertDialogType?, posCallback: () -> Unit){
        preference?.setOnPreferenceClickListener { _ ->
            if (dialogType != null) showDialog(dialogType, positiveClick = { posCallback() })
            else posCallback()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setupSwitch(switchPref: SwitchPreference?, dialogType: FileAlertDialogType?, posCallback: () -> Unit, extraCond: Boolean? = true){
        switchPref?.setOnPreferenceChangeListener { _, _ ->
            if (dialogType != null && extraCond == true){
                showDialog(dialogType, positiveClick = { posCallback() }, {
                    switchPref.isChecked = !switchPref.isChecked
                })
            }
            else posCallback()

            switchPref.isChecked = true
            return@setOnPreferenceChangeListener true
        }
    }

    private fun toggleSharedSpace(shared: Boolean?){

    }

    private fun showAuthKey(){

    }

    private fun generateAuthKey(){

    }

    private fun toggleUsersInvite(allowed: Boolean?){

    }

    private fun quitSpace(){

    }

    private fun deleteSpace(){

    }

    private fun kickAllUsers(){

    }

    private fun changeWriteAccess(state: Boolean?) {

    }

}
