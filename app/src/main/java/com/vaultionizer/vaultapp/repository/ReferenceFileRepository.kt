package com.vaultionizer.vaultapp.repository

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.service.ReferenceFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReferenceFileRepository @Inject constructor(val referenceFileService: ReferenceFileService, val gson: Gson, val localSpaceDao: LocalSpaceDao) {


    suspend fun downloadReferenceFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val response = referenceFileService.downloadReferenceFile(DownloadReferenceFileRequest(space.remoteId))

            when(response) {
                is ApiResult.Success -> {
                    val localSpace = localSpaceDao.getSpaceById(space.id)
                    localSpace?.let {
                        it.referenceFile = gson.toJson(response.data)
                        localSpaceDao.updateSpaces(localSpace)
                    }

                    emit(ManagedResult.Success(response.data))
                }
                is ApiResult.Error -> {
                    emit(ManagedResult.Error(response.statusCode))
                }
                is ApiResult.NetworkError -> {
                    emit(ManagedResult.NetworkError(response.exception))
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}