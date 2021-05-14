package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsActionEnum {
    LOGOUT,
    DELETE_USER,
    DELETE_LOCAL_FILES,
    QUIT_ALL_SPACES
}

data class SettingsAction(
    val action: SettingsActionEnum,
    val success: Boolean,
    val done: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val fileRepository: FileRepository,
    val authRepository: AuthRepository,
    val spaceRepository: SpaceRepository,
    val authCache: AuthCache
) : ViewModel() {

    private val _settingsAction = MutableLiveData<SettingsAction>()
    val settingsActionResult: LiveData<SettingsAction> = _settingsAction

    fun logout() {
        viewModelScope.launch {
            startAction(SettingsActionEnum.LOGOUT)
            val res = authRepository.logout()
            actionConditional(SettingsActionEnum.LOGOUT, res)
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            startAction(SettingsActionEnum.DELETE_USER)
            val res = authRepository.deleteUser()
            actionConditional(SettingsActionEnum.DELETE_USER, res)
        }

    }

    fun deleteLocalFiles() {
        viewModelScope.launch {
            startAction(SettingsActionEnum.DELETE_LOCAL_FILES)
            val userId = authCache.loggedInUser?.localUser?.userId
            if (userId == null) {
                actionConditional(SettingsActionEnum.DELETE_LOCAL_FILES, false)
                return@launch
            }
            fileRepository.clearLocalFiles(userId)
            actionSucceeded(SettingsActionEnum.DELETE_LOCAL_FILES)
        }
    }

    fun quitAllSpaces() {
        viewModelScope.launch {
            startAction(SettingsActionEnum.QUIT_ALL_SPACES)
            val res = spaceRepository.quitAllSpaces()
            actionConditional(SettingsActionEnum.QUIT_ALL_SPACES, res)
        }
    }

    private fun startAction(action: SettingsActionEnum) {
        _settingsAction.value = SettingsAction(action, false, false)
    }

    private fun actionConditional(action: SettingsActionEnum, res: Boolean) {
        _settingsAction.value = SettingsAction(action, res, true)
    }

    private fun actionSucceeded(action: SettingsActionEnum) {
        _settingsAction.value = SettingsAction(action, true, true)
    }
}