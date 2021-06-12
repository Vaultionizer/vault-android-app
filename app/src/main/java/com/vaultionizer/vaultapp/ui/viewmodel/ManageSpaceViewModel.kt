package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.data.model.rest.request.ChangeAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.request.ConfigureSpaceRequest
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.util.AuthKeyGen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageSpaceViewModel @Inject constructor(
    val spaceService: SpaceService
) : ViewModel() {
    private val __spaceConfig = MutableLiveData<ConfigureSpaceRequest>()
    val spaceConfig: LiveData<ConfigureSpaceRequest> = __spaceConfig


    var spaceID: Long = -1

    fun generateAuthKey() {
        val newAuthKey: String = AuthKeyGen().generateAuthKey()
        viewModelScope.launch {
            spaceService.changeAuthKey(
                ChangeAuthKeyRequest(newAuthKey),
                spaceID
            )
        }
    }

    fun toggleUsersInvite(allowed: Boolean?) {
        if (allowed == null || spaceConfig.value == null) return
        __spaceConfig.value = ConfigureSpaceRequest(
            spaceConfig.value!!.usersWriteAccess,
            allowed,
            spaceConfig.value!!.sharedSpace
        )
    }

    fun kickAllUsers() {
        viewModelScope.launch { spaceService.kickAllUsers(spaceID) }
    }

    fun changeWriteAccess(state: Boolean?) {
        if (state == null || spaceConfig.value == null) return
        __spaceConfig.value = ConfigureSpaceRequest(
            state,
            spaceConfig.value!!.usersAuthAccess,
            spaceConfig.value!!.sharedSpace
        )
    }

    fun toggleSharedSpace(shared: Boolean?) {
        if (shared == null || spaceConfig.value == null) return
        __spaceConfig.value = ConfigureSpaceRequest(
            spaceConfig.value!!.usersWriteAccess,
            spaceConfig.value!!.usersAuthAccess,
            shared
        )
    }

    fun configureSpace() {
        if (spaceConfig.value == null) return
        viewModelScope.launch { spaceService.configureSpace(spaceConfig.value!!, spaceID) }
    }
}