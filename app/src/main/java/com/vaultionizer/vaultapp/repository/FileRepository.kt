package com.vaultionizer.vaultapp.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.FileService
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.getFileName
import com.vaultionizer.vaultapp.worker.FileEncryptionWorker
import com.vaultionizer.vaultapp.worker.FileUploadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FileRepository @Inject constructor(
    val applicationContext: Context,
    val gson: Gson,
    val referenceFileRepository: ReferenceFileRepository,
    val spaceRepository: SpaceRepository,
    val localFileDao: LocalFileDao,
    val localSpaceDao: LocalSpaceDao,
    val fileExchangeService: FileExchangeService,
    val fileService: FileService,
    val syncRequestService: SyncRequestService
) {

    /*
    Simple in memory cache
    Maps localSpaceId to Root folder of file tree
     */
    private val cache = mutableMapOf<Long, VNFile>()
    private val minimumIdCache = mutableMapOf<Long, Long>()

    suspend fun getFileTree(space: VNSpace): Flow<ManagedResult<VNFile>> {
        return flow {
            if (cache[space.id] != null) {
                emit(ManagedResult.Success(cache[space.id]!!))
                return@flow
            }
            val referenceFile = referenceFileRepository.downloadReferenceFile(space)

            referenceFile.collect {
                when (it) {
                    is ManagedResult.Success -> {
                        val localFiles =
                            localSpaceDao.getSpaceWithFiles(space.id).files.map { it.remoteFileId to it }
                                .toMap()

                        Log.e(
                            "Vault",
                            "GOT ${localFiles.size} DB ${localSpaceDao.getSpaceWithFiles(space.id).files.size}"
                        )
                        localFiles.forEach {
                            Log.e(
                                "Vault",
                                "FOUND ENTRY FOR SPACE: ${space.id} ${it.key} with ${it.value}"
                            )
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

    fun uploadFile(
        space: VNSpace,
        uri: Uri,
        parent: VNFile?,
    ) {
        val uploadRequest = syncRequestService.createUploadRequest(space.id, uri)
        val workManager = WorkManager.getInstance(applicationContext)
        val workData = workDataOf(
            Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
            Constants.WORKER_FILE_PARENT_ID to (parent?.localId ?: -1),
            Constants.WORKER_SPACE_ID to space.id
        )

        val encryptionWorker =
            OneTimeWorkRequestBuilder<FileEncryptionWorker>().setInputData(workData)
                .addTag(Constants.WORKER_TAG_FILE)
                .build()
        val uploadWorker =
            OneTimeWorkRequestBuilder<FileUploadWorker>().setInputData(workData)
                .addTag(Constants.WORKER_TAG_FILE).build()

        parent?.content?.add(
            VNFile(
                getFileName(uri, applicationContext.contentResolver) ?: "UNKNOWN", space, parent
            )
        )

        // TODO(jatsqi): Persist file locally
        workManager
            .beginWith(encryptionWorker)
            .then(uploadWorker)
            .enqueue()
    }


    fun uploadFolder(
        space: VNSpace,
        name: String,
        parent: VNFile
    ): Flow<ManagedResult<VNFile>> {
        return flow {
            if (!minimumIdCache.containsKey(space.id)) {
                minimumIdCache[space.id] = -2
            } else {
                minimumIdCache[space.id] = minimumIdCache[space.id]!! - 1
            }

            Log.e("Vault", "MIN ID ${minimumIdCache[space.id]!!}")
            val folder = VNFile(
                name,
                space,
                parent,
                minimumIdCache[space.id]!!,
                null,
                mutableListOf()
            ).apply {
                lastUpdated = System.currentTimeMillis()
                createdAt = System.currentTimeMillis()
            }
            parent.content!!.add(folder)

            resyncRefFile(space).collect {
                when (it) {
                    is ManagedResult.Success -> {
                        emit(ManagedResult.Success(folder))
                    }
                    else -> {
                        parent.content!!.remove(folder)
                        emit(ManagedResult.Error(400)) // TODO(jatsqi): Error handling
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun announceUpload(spaceId: Long): Flow<ManagedResult<Long>> {
        return flow {
            when (val response = fileService.uploadFile(
                UploadFileRequest(
                    1,
                    (spaceRepository.getSpace(spaceId)
                        .first() as ManagedResult.Success<VNSpace>).data.remoteId
                )
            )) {
                is ApiResult.Success -> {
                    emit(ManagedResult.Success(response.data))
                }
                is ApiResult.NetworkError -> {
                    emit(ManagedResult.NetworkError(response.exception))
                }
                is ApiResult.Error -> {
                    emit(ManagedResult.Error(response.statusCode))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFile(file: VNFile): Flow<ManagedResult<VNFile>> {
        return flow {
            if (file.parent != null) {
                file.parent.content?.remove(file)
                resyncRefFile(file.space).collect {
                    when (it) {
                        is ManagedResult.Success -> {
                            emit(ManagedResult.Success(file))
                        }
                        else -> {
                            Log.e("Vault", it.javaClass.name)
                            emit(ManagedResult.Error(400)) // TODO(jatsqi): Error handling
                        }
                    }
                }
            }
        }
    }

    fun cacheEvict(spaceId: Long) {
        cache.remove(spaceId)
        minimumIdCache.remove(spaceId)
    }


    private fun buildTree(
        elements: List<NetworkElement>?,
        localFiles: Map<Long, LocalFile>,
        parent: VNFile,
        space: VNSpace,
        ctx: Context
    ) {
        elements?.forEach {
            if (minimumIdCache[space.id]!! > it.id) minimumIdCache[space.id] = it.id

            if (it is NetworkFolder) {
                val folder = VNFile(
                    it.name,
                    space,
                    parent,
                    localId = it.id,
                    content = mutableListOf()
                ).apply {
                    createdAt = it.createdAt
                }
                buildTree(it.content, localFiles, folder, space, ctx)

                parent.content!!.add(folder)
            } else if (it is NetworkFile) {
                val add = VNFile(
                    it.name,
                    space,
                    parent,
                    localId = localFiles[it.id]?.fileId,
                    remoteId = it.id
                )
                add.createdAt = it.createdAt
                add.lastUpdated = it.updatedAt

                if (!add.isDownloaded(ctx) && add.localId != null) {
                    Log.e("Vault", "LocalID ${add.localId} localFiles ${localFiles[add.localId]}")
                    localFileDao.deleteFiles(localFiles[add.localId]!!)
                    add.localId = null

                    Log.e("Vault", "Delete local mismatch!")
                }

                parent.content!!.add(add)
            }
        }
    }

    private suspend fun resyncRefFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            if (!cache.containsKey(space.id)) {
                emit(ManagedResult.ConsistencyError)
                return@flow
            }

            val root = cache[space.id]!!.mapToNetwork() as NetworkFolder

            val referenceFile = NetworkReferenceFile(
                1,
                root.content ?: mutableListOf()
            )

            Log.e("Vault", "SYNC")
            emit(referenceFileRepository.uploadReferenceFile(referenceFile, space).first())
        }.flowOn(Dispatchers.IO)
    }

}