package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(
        host: String,
        username: String,
        password: String
    ): Flow<Resource<LoggedInUser>>

    suspend fun register(
        host: String,
        username: String,
        password: String,
        authKey: String
    ): Flow<Resource<LoggedInUser>>

    suspend fun logout(): Boolean

    suspend fun deleteUser(): Boolean
}