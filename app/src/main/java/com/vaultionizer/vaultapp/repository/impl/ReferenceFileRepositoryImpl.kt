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
import com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
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

    private val cachedReferenceFiles = mutableMapOf<Long, Pair<NetworkReferenceFile, Long>>()

    override suspend fun downloadReferenceFile(space: VNSpace): Flow<Resource<NetworkReferenceFile>> {
        return object : NetworkBoundResource<NetworkReferenceFile, NetworkReferenceFile>() {
            override fun shouldFetch(): Boolean {
                val cachedRefFile = cachedReferenceFiles[space.id] ?: return true
                return (System.currentTimeMillis() - cachedRefFile.second) > 1000 * 60 * 5
            }

            override suspend fun fromDb(): Resource<NetworkReferenceFile> {
                val cachedRefFile =
                    cachedReferenceFiles[space.id]?.first ?: return Resource.ConsistencyError
                return Resource.Success(cachedRefFile)
            }

            override suspend fun saveToDb(networkResult: NetworkReferenceFile) {
                cachedReferenceFiles[space.id] = Pair(networkResult, System.currentTimeMillis())
            }

            override suspend fun fromNetwork(): ApiResult<NetworkReferenceFile> {
                return referenceFileService.downloadReferenceFile(DownloadReferenceFileRequest(space.remoteId))
            }

            override fun dispatchError(result: ApiResult<NetworkReferenceFile>): Resource<NetworkReferenceFile> {
                return Resource.Error(0)
            }

        }.asFlow()
    }

    override suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<Resource<NetworkReferenceFile>> {
        return flow {
            val response = referenceFileService.uploadReferenceFile(
                UploadReferenceFileRequest(
                    gson.toJson(referenceFile), space.remoteId
                )
            )
            when (response) {
                is ApiResult.Success -> {
                    emit(Resource.Success(referenceFile))
                }
                is ApiResult.Error -> {
                    emit(Resource.RefFileError.RefFileUploadError)
                }
                is ApiResult.NetworkError -> {
                    Log.e(
                        "Vault",
                        "Network error from upload ${response.exception.localizedMessage}"
                    )
                    emit(Resource.NetworkError(response.exception))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncReferenceFile(
        spaceId: Long,
        root: VNFile
    ): Flow<Resource<NetworkReferenceFile>> {
        return flow {
            val spaceResult = spaceRepository.getSpace(spaceId)
            spaceResult.collect {
                when (it) {
                    is Resource.Success -> {
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
                        emit(Resource.Error(9))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}