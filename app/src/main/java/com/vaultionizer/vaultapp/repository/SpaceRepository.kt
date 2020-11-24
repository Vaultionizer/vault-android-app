package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.space.SpaceEntry
import com.vaultionizer.vaultapp.service.SpaceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.internal.toImmutableMap
import javax.inject.Inject

class SpaceRepository @Inject constructor(val spaceService: SpaceService) {

    private val spaceCache = mutableMapOf<Long, SpaceEntry>()

    suspend fun getAllSpaces(): Flow<ManagedResult<List<SpaceEntry>>> {
        return flow {
            val response = spaceService.getAll()

            when(response) {
                is ApiResult.Success -> {
                    for (space in response.data) {
                        spaceCache[space.spaceID] = space
                    }

                    emit(ManagedResult.Success(response.data))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getCachedSpaces() = spaceCache.toImmutableMap()

}