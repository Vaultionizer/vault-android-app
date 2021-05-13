package com.vaultionizer.vaultapp.repository.impl

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.UploadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.ReferenceFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
        return flow {
            if (cachedReferenceFiles[space.id] != null) {
                if (System.currentTimeMillis() - cachedReferenceFiles[space.id]!!.second < 1000 * 60 * 5) {
                    emit(Resource.Success(cachedReferenceFiles[space.id]!!.first))
                    return@flow
                }
            }

            val downloadResponse =
                referenceFileService.downloadReferenceFile(DownloadReferenceFileRequest(space.remoteId))

            if (downloadResponse is ApiResult.Success) {
                cachedReferenceFiles[space.id] =
                    Pair(downloadResponse.data, System.currentTimeMillis())
            }

            emit(downloadResponse.mapToResource())
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<Resource<NetworkReferenceFile>> {
        return flow {
            val uploadResponse = referenceFileService.uploadReferenceFile(
                UploadReferenceFileRequest(
                    gson.toJson(referenceFile), space.remoteId
                )
            )

            emit(uploadResponse.mapToResource(referenceFile))
        }
    }

    override suspend fun syncReferenceFile(
        root: VNFile
    ): Flow<Resource<NetworkReferenceFile>> {
        val networkRoot = root.mapToNetwork() as NetworkFolder
        return uploadReferenceFile(
            NetworkReferenceFile(
                0,
                networkRoot.content ?: mutableListOf()
            ), root.space
        )
    }
}