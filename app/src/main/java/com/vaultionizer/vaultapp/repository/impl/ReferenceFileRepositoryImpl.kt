package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.UploadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.ReferenceFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReferenceFileRepositoryImpl @Inject constructor(
    val referenceFileService: ReferenceFileService,
    val gson: Gson,
    val localSpaceDao: LocalSpaceDao,
    val spaceRepository: SpaceRepository
) : ReferenceFileRepository {

    override suspend fun downloadReferenceFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val response =
                referenceFileService.downloadReferenceFile(DownloadReferenceFileRequest(space.remoteId))

            when (response) {
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

    override suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val response = referenceFileService.uploadReferenceFile(
                UploadReferenceFileRequest(
                    gson.toJson(referenceFile), space.remoteId
                )
            )
            when (response) {
                is ApiResult.Success -> {
                    emit(ManagedResult.Success(referenceFile))
                }
                is ApiResult.Error -> {
                    emit(ManagedResult.RefFileError.RefFileUploadError)
                }
                is ApiResult.NetworkError -> {
                    Log.e(
                        "Vault",
                        "Network error from upload ${response.exception.localizedMessage}"
                    )
                    emit(ManagedResult.NetworkError(response.exception))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncReferenceFile(
        spaceId: Long,
        root: VNFile
    ): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val spaceResult = spaceRepository.getSpace(spaceId)
            spaceResult.collect {
                when (it) {
                    is ManagedResult.Success -> {
                        val networkRoot = root.mapToNetwork() as NetworkFolder
                        val result = uploadReferenceFile(
                            NetworkReferenceFile(
                                0,
                                networkRoot.content ?: mutableListOf()
                            ),
                            it.data
                        )

                        result.collect {
                            emit(it)
                        }
                    }
                    else -> {
                        // TODO(jatsqi): Better error handling.
                        emit(ManagedResult.Error(9))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}