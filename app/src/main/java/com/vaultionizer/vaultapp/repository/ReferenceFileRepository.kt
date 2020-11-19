package com.vaultionizer.vaultapp.repository

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.rf.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.rf.ReferenceFile
import com.vaultionizer.vaultapp.service.ReferenceFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReferenceFileRepository @Inject constructor(val referenceFileService: ReferenceFileService, val gson: Gson) {

    private val cachedFiles = mutableMapOf<Long, ReferenceFile>()

    suspend fun downloadReferenceFile(spaceId: Long): Flow<ManagedResult<ReferenceFile>> {
        return flow {
            val response = referenceFileService.downloadReferenceFile(DownloadReferenceFileRequest(spaceId))

            when(response) {
                is ApiResult.Success -> {
                    emit(ManagedResult.Success(response.data))

                    cachedFiles[spaceId] = response.data
                }
                is ApiResult.Error -> {
                    emit(ManagedResult.Error(response.statusCode))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadReferenceFile(spaceId: Long) {
        TODO()
    }

    fun getCachedReferenceFile(spaceId: Long) = cachedFiles[spaceId]

}