package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.data.model.rest.request.ConfigureSpaceRequest
import com.vaultionizer.vaultapp.service.SpaceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageSpaceViewModel @Inject constructor(
    val spaceService: SpaceService
) : ViewModel() {
    var spaceID: Long = -1

    fun generateAuthKey() {

    }

    fun toggleUsersInvite(allowed: Boolean?) {

    }

    fun quitSpace() {

    }

    fun deleteSpace() {

    }

    fun kickAllUsers() {
        viewModelScope.launch { spaceService.kickAllUsers(spaceID) }
    }

    fun changeWriteAccess(state: Boolean?) {
        viewModelScope.launch { spaceService.configureSpace(ConfigureSpaceRequest()) }
    }

    fun toggleSharedSpace(shared: Boolean?) {

    }
}