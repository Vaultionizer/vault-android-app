package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalUser
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.request.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import com.vaultionizer.vaultapp.data.model.rest.user.NetworkUserAuthPair
import com.vaultionizer.vaultapp.hilt.RestModule
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.UserService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.extension.hashSha512
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    val userService: UserService,
    val gson: Gson,
    val localUserDao: LocalUserDao,
    val authCache: AuthCache,
    val spaceRepository: SpaceRepository
) : AuthRepository {

    override suspend fun login(
        host: String,
        username: String,
        password: String
    ): Flow<Resource<LoggedInUser>> {
        RestModule.host = "${Constants.DEFAULT_PROTOCOL}://$host"

        return object : NetworkBoundResource<LoggedInUser, NetworkUserAuthPair>() {
            override fun shouldFetch(): Boolean = true

            override suspend fun fromDb(): Resource<LoggedInUser> {
                throw RuntimeException("Not reachable!")
            }

            override suspend fun saveToDb(networkResult: NetworkUserAuthPair) {
                updateLocalUser(username, host, networkResult)
            }

            override suspend fun fromNetwork(): ApiResult<NetworkUserAuthPair> {
                return userService.loginUser(LoginUserRequest(username, password.hashSha512()))
            }

        }.asFlow()
    }

    override suspend fun register(
        host: String,
        username: String,
        password: String,
        authKey: String
    ): Flow<Resource<LoggedInUser>> {
        RestModule.host = "${Constants.DEFAULT_PROTOCOL}://$host"

        return object : NetworkBoundResource<LoggedInUser, NetworkUserAuthPair>() {
            override fun shouldFetch(): Boolean = true

            override suspend fun fromDb(): Resource<LoggedInUser> {
                throw RuntimeException("Not reachable.")
            }

            override suspend fun saveToDb(networkResult: NetworkUserAuthPair) {
                updateLocalUser(username, host, networkResult)
            }

            override suspend fun fromNetwork(): ApiResult<NetworkUserAuthPair> {
                return userService.createUser(
                    CreateUserRequest(
                        password.hashSha512(), gson.toJson(
                            NetworkReferenceFile.EMPTY_FILE
                        ), username
                    )
                )
            }

            override fun transformOnSuccess(apiResult: NetworkUserAuthPair): LoggedInUser {
                return authCache.loggedInUser!!
            }

            override fun dispatchError(result: ApiResult.Error): Resource<LoggedInUser> {
                when (result.statusCode) {
                    409 -> return Resource.UserError.UsernameAlreadyInUseError
                    408 -> return Resource.UserError.ValueConstraintsError
                }

                return super.dispatchError(result)
            }

        }.asFlow()
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
            authCache.loggedInUser =
                LoggedInUser(localUser, authPair.sessionKey, authPair.websocketToken)
        }
    }

    override suspend fun logout(): Boolean {
        if (authCache.loggedInUser != null){
            authCache.loggedInUser = null
            userService.logoutUser()
            //if(response is ApiResult.Success ) {
            return true
            // }
        }
        return false
    }

    override suspend fun deleteUser(): Boolean {
        if (authCache.loggedInUser?.localUser?.remoteUserId != null){
            spaceRepository.deleteAllSpaces()
            userService.deleteUser()
            //if (response is ApiResult.Success){
            return true
            //}
        }
        return false
    }
}