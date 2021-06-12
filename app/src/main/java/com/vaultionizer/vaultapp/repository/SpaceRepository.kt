package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.db.entity.SharedSpaceSecret
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {
    suspend fun getAllSpaces(): Flow<Resource<List<VNSpace>>>

    suspend fun getSpace(spaceId: Long): Flow<Resource<VNSpace>>

    suspend fun getSpaceRemoteId(spaceId: Long): Long?

    suspend fun peekNextSpaceId(): Long

    suspend fun createSpace(
        name: String,
        isPrivate: Boolean,
        writeAccess: Boolean,
        authKeyAccess: Boolean,
        algorithm: String,
        password: String? = null
    ): Flow<Resource<VNSpace>>

    suspend fun deleteSpace(space: VNSpace): Flow<Resource<VNSpace>>

    suspend fun quitAllSpaces(): Boolean

    suspend fun deleteAllSpaces(): Boolean

    suspend fun getSpaceSecret(spaceId: Long): Flow<Resource<SharedSpaceSecret>>

    suspend fun joinSpace(remoteSpaceId: Long, spaceId: Long, authKey: String): Flow<Resource<Boolean>>
}