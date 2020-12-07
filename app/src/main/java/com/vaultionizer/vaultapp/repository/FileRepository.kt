package com.vaultionizer.vaultapp.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.writeFileToInternal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.min

class FileRepository @Inject constructor(
    val applicationContext: Context,
    val gson: Gson,
    val referenceFileRepository: ReferenceFileRepository,
    val spaceRepository: SpaceRepository,
    val localFileDao: LocalFileDao,
    val localSpaceDao: LocalSpaceDao,
    val fileExchangeService: FileExchangeService) {

    /*
    Simple in memory cache
    Maps localSpaceId to Root folder of file tree
     */
    private val cache = mutableMapOf<Long, VNFile>()
    private val minimumIdCache = mutableMapOf<Long, Long>()

    suspend fun getFileTree(space: VNSpace): Flow<ManagedResult<VNFile>> {
        return flow {
            if(cache[space.id] != null) {
                emit(ManagedResult.Success(cache[space.id]!!))
                return@flow
            }
            val referenceFile = referenceFileRepository.downloadReferenceFile(space)

            referenceFile.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        val localFiles = localSpaceDao.getSpaceWithFiles(space.id).files.map { it.remoteFileId to it }.toMap()

                        Log.e("Vault", "GOT ${localFiles.size} DB ${localSpaceDao.getSpaceWithFiles(space.id).files.size}")
                        localFiles.forEach {
                            Log.e("Vault", "FOUND ENTRY FOR SPACE: ${space.id} ${it.key} with ${it.value}")
                        }

                        val root = VNFile(
                            name = "/",
                            space = space,
                            parent = null,
                            localId = -1,
                            content = mutableListOf()
                        )

                        cache[space.id] = root
                        minimumIdCache[space.id] = -1
                        buildTree(it.data.elements, localFiles, root, space, applicationContext)
                        emit(ManagedResult.Success(root))
                    }
                    else -> {
                        emit(ManagedResult.Error((it as ApiResult.Error).statusCode))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun buildTree(elements: List<NetworkElement>?, localFiles: Map<Long, LocalFile>, parent: VNFile, space: VNSpace, ctx: Context) {
        elements?.forEach {
            if(minimumIdCache[space.id]!! > it.id) minimumIdCache[space.id] = it.id

            if(it is NetworkFolder) {
                val folder = VNFile(it.name, space, parent, localId = it.id, content = mutableListOf()).apply {
                    createdAt = it.createdAt
                }
                buildTree(it.content, localFiles, folder, space, ctx)

                parent.content!!.add(folder)
            } else if(it is NetworkFile) {
                val add = VNFile(
                            it.name,
                            space,
                            parent,
                            localId = localFiles[it.id]?.fileId,
                            remoteId = it.id)
                add.createdAt = it.createdAt
                add.lastUpdated = it.updatedAt

                if(!add.isDownloaded(ctx) && add.localId != null) {
                    Log.e("Vault", "LocalID ${add.localId} localFiles ${localFiles[add.localId]}")
                    localFileDao.deleteFiles(localFiles[add.localId]!!)
                    add.localId = null

                    Log.e("Vault", "Delete local mismatch!")
                }

                parent.content!!.add(add)
            }
        }
    }

    suspend fun uploadFile(space: VNSpace, parent: VNFile?, data: ByteArray, name: String, context: Context): Flow<VNFile> {
        return flow {
            val cachedList = cache[space.id]
            fileExchangeService.uploadFile(space.remoteId, data).collect {
                if(it is ApiResult.Success) {
                    val localFile = LocalFile(
                        0,
                        space.id,
                        it.data
                    )

                    val localFileId = localFileDao.createFile(localFile)
                    val newFile = VNFile(
                        name,
                        space,
                        parent,
                        remoteId = it.data,
                        localId = localFileId,
                    ).apply {
                        createdAt = System.currentTimeMillis()
                        lastUpdated = System.currentTimeMillis()
                        lastSyncTimestamp = System.currentTimeMillis()
                        state = VNFile.State.AVAILABLE_OFFLINE
                    }

                    writeFileToInternal(context, "${localFileId}.${Constants.VN_FILE_SUFFIX}", data)
                    parent?.content?.add(newFile)

                    resyncRefFile(space)
                    emit(newFile)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadFolder(space: VNSpace, name: String, parent: VNFile): Flow<VNFile> {
        return flow {
            if(!minimumIdCache.containsKey(space.id)) {
                minimumIdCache[space.id] = -2
            } else {
                minimumIdCache[space.id] = minimumIdCache[space.id]!! - 1
            }

            Log.e("Vault", "MIN ID ${minimumIdCache[space.id]!!}")
            val folder = VNFile(name, space, parent, minimumIdCache[space.id]!!, null, mutableListOf()).apply {
                lastUpdated = System.currentTimeMillis()
                createdAt = System.currentTimeMillis()
            }
            parent.content!!.add(folder)

            resyncRefFile(space) // TODO(jatsqi) Error handling
            emit(folder)
        }
    }

    fun cacheEvict(spaceId: Long) {
        cache.remove(spaceId)
        minimumIdCache.remove(spaceId)
    }

    private suspend fun resyncRefFile(space: VNSpace) {
        if(!cache.containsKey(space.id)) return
        val root = cache[space.id]!!.mapToNetwork() as NetworkFolder

        val referenceFile = NetworkReferenceFile(
            1,
            root.content ?: mutableListOf()
        )

        Log.e("Vault", "SYNC")
        referenceFileRepository.uploadReferenceFile(referenceFile, space).collect()
    }

}