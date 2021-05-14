package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {
    suspend fun getAllSpaces(): Flow<ManagedResult<List<VNSpace>>>

    suspend fun getSpace(spaceId: Long): Flow<ManagedResult<VNSpace>>

    suspend fun getSpaceRemoteId(spaceId: Long): Long?

    suspend fun createSpace(name: String, isPrivate: Boolean): Flow<ManagedResult<VNSpace>>

    suspend fun deleteSpace(space: VNSpace): Flow<ManagedResult<VNSpace>>

    suspend fun quitAllSpaces(): Boolean

    suspend fun deleteAllSpaces(): Boolean
}