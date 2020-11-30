package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkReferenceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FileRepository @Inject constructor(
    val referenceFileRepository: ReferenceFileRepository,
    val spaceRepository: SpaceRepository,
    val localFileDao: LocalFileDao,
    val localSpaceDao: LocalSpaceDao) {

    private val cache = mutableMapOf<Long, Map<Long, VNFile>>()

    suspend fun getSpaceFiles(space: VNSpace): Flow<ManagedResult<Map<Long, VNFile>>> {
        return flow {
            if(cache[space.id] != null) {
                emit(ManagedResult.Success(cache[space.id]!!))
                return@flow
            }
            val referenceFile = flow { emit(ManagedResult.Success(NetworkReferenceFile.generateRandom())) } //referenceFileRepository.downloadReferenceFile(space)

            referenceFile.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        val localFiles = localSpaceDao.getSpaceWithFiles(space.id).files.map { it.remoteFileId to it }.toMap()

                        val map = mutableMapOf<Long, VNFile>()
                        map[NetworkReferenceFile.GLOBAL_FOLDER_ID_COUNTER--] = VNFile(
                            "/",
                            localId = NetworkReferenceFile.GLOBAL_FOLDER_ID_COUNTER
                        )

                        buildTree(it.data.elements, map, NetworkReferenceFile.GLOBAL_FOLDER_ID_COUNTER)
                        localFiles.keys.forEach {
                            val file = map[it]
                            if(file != null) {
                                file.localId = it
                            }
                        }

                        cache[space.id] = map
                        emit(ManagedResult.Success(map))
                    }
                    else -> {
                        emit(ManagedResult.Error((it as ApiResult.Error).statusCode))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun buildTree(elements: List<NetworkElement>?, map: MutableMap<Long, VNFile>, parent: Long?) {
        elements?.forEach {
            if(it is NetworkFolder) {
                val folder = VNFile(it.name, localId = it.id).apply {
                    parentId = parent
                }

                map[folder.localId!!] = folder
                buildTree(it.content, map, folder.localId)
            } else if(it is NetworkFile) {
                map[it.id] = VNFile(it.name, remoteId = it.id).apply {
                    createdAt = it.createdAt
                    lastUpdated = it.updatedAt
                    parentId = parent
                }
            }
        }
    }

}