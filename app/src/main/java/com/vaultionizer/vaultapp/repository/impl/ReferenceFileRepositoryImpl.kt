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
import com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.ReferenceFileService
import kotlinx.coroutines.flow.Flow
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
        }.asFlow()
    }

    override suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<Resource<NetworkReferenceFile>> {
        return object : NetworkBoundResource<NetworkReferenceFile, Unit>() {
            override fun shouldFetch(): Boolean = true

            override suspend fun fromDb(): Resource<NetworkReferenceFile> {
                throw RuntimeException("Not reachable.")
            }

            override suspend fun saveToDb(networkResult: Unit) {}

            override suspend fun fromNetwork(): ApiResult<Unit> {
                return referenceFileService.uploadReferenceFile(
                    UploadReferenceFileRequest(
                        gson.toJson(referenceFile), space.remoteId
                    )
                )
            }

            override fun dispatchError(result: ApiResult.Error): Resource<NetworkReferenceFile> {
                return Resource.RefFileError.RefFileUploadError
            }
        }.asFlow()
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