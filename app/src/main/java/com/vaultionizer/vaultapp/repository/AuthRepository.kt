package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.db.entity.LocalUser
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(
        host: String,
        username: String,
        password: String
    ): Flow<ManagedResult<LoggedInUser>>

    suspend fun register(
        host: String,
        username: String,
        password: String,
        authKey: String
    ): Flow<ManagedResult<LoggedInUser>>

    suspend fun logout(): Boolean

    suspend fun deleteUser(): Boolean
}