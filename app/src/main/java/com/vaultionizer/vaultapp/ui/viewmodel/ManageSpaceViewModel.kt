package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.data.db.dao.SharedSpaceSecretDao
import com.vaultionizer.vaultapp.data.model.rest.request.ChangeAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.request.ConfigureSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.util.AuthKeyGen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AuthKeySecretPair(
    val authKey: String,
    val secret: String
)

@HiltViewModel
class ManageSpaceViewModel @Inject constructor(
    val spaceService: SpaceService,
    val spaceRepository: SpaceRepository,
    val sharedSpaceSecretDao: SharedSpaceSecretDao
) : ViewModel() {
    private val __spaceConfig = MutableLiveData<ConfigureSpaceRequest>()
    val spaceConfig: LiveData<ConfigureSpaceRequest> = __spaceConfig

    private val __authKey = MutableLiveData<AuthKeySecretPair>()
    val authKey: LiveData<AuthKeySecretPair> = __authKey


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

    fun getSharedSpaceSecret(): ByteArray{
        return ByteArray(10)
    }

    fun getAuthKey(remoteSpaceId: Long){

        viewModelScope.launch {
            var secret : String? = null
            spaceRepository.getSpaceSecret(spaceID).collect {
                when(it){
                    is Resource.Success -> {
                        secret = Base64.encodeToString(it.data.secret, Base64.NO_WRAP)
                    }
                    else -> {
                    }
                }
            }
            if (secret != null) {
                val res = spaceService.getAuthKey(remoteSpaceId)
                when (res) {
                    is ApiResult.Success -> {
                        __authKey.value = AuthKeySecretPair(res.data.authKey, secret!!)
                    }
                    else -> {
                        // TODO: handle error
                    }
                }
            }
        }

    }
}