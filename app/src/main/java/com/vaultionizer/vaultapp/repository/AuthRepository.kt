package com.vaultionizer.vaultapp.repository

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalUser
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.user.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import com.vaultionizer.vaultapp.data.model.rest.user.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.user.NetworkUserAuthPair
import com.vaultionizer.vaultapp.service.UserService
import com.vaultionizer.vaultapp.hilt.RestModule
import com.vaultionizer.vaultapp.util.extension.hashSha512
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepository @Inject constructor(val userService: UserService, val gson: Gson, val localUserDao: LocalUserDao) {

    companion object {
        var user: LoggedInUser? = null
            private set

        val isLoggedIn: Boolean
            get() = user != null
    }

    suspend fun login(host: String, username: String, password: String): Flow<ManagedResult<LoggedInUser>> {
        RestModule.host = "https://$host"

        return flow {
            val response = userService.loginUser(LoginUserRequest(username, password.hashSha512()))

            when(response) {
                is ApiResult.Success -> {
                    updateLocalUser(username, host, response.data)

                    emit(ManagedResult.Success(user!!))
                }
                is ApiResult.NetworkError -> {
                    emit(ManagedResult.NetworkError(response.exception))
                }
                is ApiResult.Error -> {
                    emit(ManagedResult.Error(statusCode = response.statusCode))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun register(host: String, username: String, password: String, authKey: String): Flow<ManagedResult<LoggedInUser>> {
        RestModule.host = "https://$host"

        return flow {
            val response = userService.createUser(CreateUserRequest(password.hashSha512(), gson.toJson(NetworkReferenceFile.EMPTY_FILE), username))

            when(response) {
                is ApiResult.Success -> {
                    updateLocalUser(username, host, response.data)

                    emit(ManagedResult.Success(user!!))
                }
                is ApiResult.NetworkError -> {
                    emit(ManagedResult.NetworkError(response.exception))
                }
                is ApiResult.Error -> {
                    if(response.statusCode == 409) {
                        emit(ManagedResult.UserError.UsernameAlreadyInUseError)
                    } else if(response.statusCode == 400) {
                        emit(ManagedResult.UserError.ValueConstraintsError)
                    } else {
                        emit(ManagedResult.Error(statusCode = response.statusCode))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun updateLocalUser(username: String, host: String, authPair: NetworkUserAuthPair) {
        if(localUserDao.getUserById(authPair.userID) == null) {
            localUserDao.createUser(LocalUser(0, authPair.userID, username, host, System.currentTimeMillis()))
        }

        val localUser = localUserDao.getUserById(authPair.userID)
        if(localUser != null) {
            localUser.lastLogin = System.currentTimeMillis()
            setLoggedInUser(LoggedInUser(localUser, authPair.sessionKey, authPair.websocketToken))
        }
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        user = loggedInUser
    }
}