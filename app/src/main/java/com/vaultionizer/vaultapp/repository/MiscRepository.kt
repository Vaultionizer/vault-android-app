package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import kotlinx.coroutines.flow.Flow

interface MiscRepository {
    suspend fun pingHost(host: String): Flow<ManagedResult<NetworkVersion>>
}