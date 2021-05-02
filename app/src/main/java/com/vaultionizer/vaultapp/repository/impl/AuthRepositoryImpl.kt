package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalUser
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.request.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import com.vaultionizer.vaultapp.data.model.rest.user.NetworkUserAuthPair
import com.vaultionizer.vaultapp.hilt.RestModule
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.service.UserService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.extension.hashSha512
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    val userService: UserService,
    val gson: Gson,
    val localUserDao: LocalUserDao
) : AuthRepository {

    override var loggedInUser: LoggedInUser? = null

    override suspend fun login(
        host: String,
        username: String,
        password: String
    ): Flow<ManagedResult<LoggedInUser>> {
        RestModule.host = "${Constants.DEFAULT_PROTOCOL}://$host"

        return flow {
            val response = userService.loginUser(LoginUserRequest(username, password.hashSha512()))

            when (response) {
                is ApiResult.Success -> {
                    updateLocalUser(username, host, response.data)

                    emit(ManagedResult.Success(loggedInUser!!))
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

    override suspend fun register(
        host: String,
        username: String,
        password: String,
        authKey: String
    ): Flow<ManagedResult<LoggedInUser>> {
        RestModule.host = "${Constants.DEFAULT_PROTOCOL}://$host"

        return flow {
            val response = userService.createUser(
                CreateUserRequest(
                    password.hashSha512(), gson.toJson(
                        NetworkReferenceFile.EMPTY_FILE
                    ), username
                )
            )

            when (response) {
                is ApiResult.Success -> {
                    updateLocalUser(username, host, response.data)

                    emit(ManagedResult.Success(loggedInUser!!))
                }
                is ApiResult.NetworkError -> {
                    emit(ManagedResult.NetworkError(response.exception))
                }
                is ApiResult.Error -> {
                    if (response.statusCode == 409) {
                        emit(ManagedResult.UserError.UsernameAlreadyInUseError)
                    } else if (response.statusCode == 400) {
                        emit(ManagedResult.UserError.ValueConstraintsError)
                    } else {
                        emit(ManagedResult.Error(statusCode = response.statusCode))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun updateLocalUser(username: String, host: String, authPair: NetworkUserAuthPair) {
        if (localUserDao.getUserByRemoteId(authPair.userID, host) == null) {
            localUserDao.createUser(
                LocalUser(
                    0,
                    authPair.userID,
                    username,
                    host,
                    System.currentTimeMillis()
                )
            )
        }

        val localUser = localUserDao.getUserByRemoteId(authPair.userID, host)
        Log.e("Vault", "Found user ${localUser?.toString()}")

        if (localUser != null) {
            localUser.lastLogin = System.currentTimeMillis()
            loggedInUser = LoggedInUser(localUser, authPair.sessionKey, authPair.websocketToken)
        }
    }

}