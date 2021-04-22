package com.vaultionizer.vaultapp.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.cache.FileCache
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
import com.vaultionizer.vaultapp.service.FileService
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.getFileName
import com.vaultionizer.vaultapp.worker.DataEncryptionWorker
import com.vaultionizer.vaultapp.worker.FileDownloadWorker
import com.vaultionizer.vaultapp.worker.FileUploadWorker
import com.vaultionizer.vaultapp.worker.ReferenceFileSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FileRepository @Inject constructor(
    val applicationContext: Context,
    val gson: Gson,
    val referenceFileRepository: ReferenceFileRepository,
    val spaceRepository: SpaceRepository,
    val localFileDao: LocalFileDao,
    val localSpaceDao: LocalSpaceDao,
    val fileService: FileService,
    val syncRequestService: SyncRequestService,
) {

    companion object {
        const val ROOT_FOLDER_ID = -1L
    }

    /**
     * Simple in memory cache for files.
     * Every spaceId maps to excactly one file cache.
     */
    private val fileCaches = mutableMapOf<Long, FileCache>()

    /**
     * In memory cache for the current minimum id of each space.
     * This id is used to determine the next id for a new folder.
     * Folder id's are ALWAYS negative to avoid any conflict with the
     * remoteFileId, because the server only knows files and is completely
     * unaware of folders.
     * TODO(jatsqi): Merge file cache with minimum id cache
     */
    private val minimumIdCache = mutableMapOf<Long, Long>()

    suspend fun getFileTree(space: VNSpace): Flow<ManagedResult<VNFile>> {
        return flow {
            val cache = fileCaches[space.id] ?: FileCache()
            cache.getFile(ROOT_FOLDER_ID)?.let {
                emit(ManagedResult.Success(it))
                return@flow
            }

            val referenceFile = referenceFileRepository.downloadReferenceFile(space)
            referenceFile.collect {
                when (it) {
                    is ManagedResult.Success -> {
                        val localFiles =
                            localSpaceDao.getSpaceWithFiles(space.id).files.filter { it.remoteFileId != null }
                                .map { it.remoteFileId!! to it }
                                .toMap()

                        val root = VNFile(
                            name = "/",
                            space = space,
                            parent = null,
                            localId = -1,
                            content = mutableListOf()
                        )

                        // TODO(jatsqi):    Add files that are being uploaded to local file tree.
                        //                  Steps:
                        //                      1) Query sync requests
                        //                      2) Add new VNFile to parent folder
                        cache.addFile(root)
                        minimumIdCache[space.id] = -1
                        buildTreeFromNetwork(
                            it.data.elements,
                            localFiles,
                            root,
                            space,
                            applicationContext
                        )
                        emit(ManagedResult.Success(root))
                    }
                    else -> {
                        emit(ManagedResult.Error((it as ManagedResult.Error).statusCode))
                    }
                }
            }

            fileCaches.put(space.id, cache)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadFile(
        space: VNSpace,
        uri: Uri,
        parent: VNFile,
    ) {
        withContext(Dispatchers.IO) {
            val workManager = WorkManager.getInstance(applicationContext)

            // Create file in DB
            val fileLocalId = localFileDao.createFile(
                LocalFile(
                    0, space.id, null
                )
            )

            // Create upload request
            val uploadRequest =
                syncRequestService.createUploadRequest(space.id, uri, fileLocalId, parent.localId!!)

            val encryptionWorkData = workDataOf(
                Constants.WORKER_SPACE_ID to space.id,
                Constants.WORKER_FILE_BYTES to applicationContext.contentResolver.openInputStream(
                    uri
                )?.readBytes()
            )
            val uploadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
                Constants.WORKER_FILE_LOCAL_ID to fileLocalId
            )
            val refWorkData = workDataOf(
                Constants.WORKER_SPACE_ID to space.id
            )

            val encryptionWorker =
                OneTimeWorkRequestBuilder<DataEncryptionWorker>().setInputData(encryptionWorkData)
                    .addTag(Constants.WORKER_TAG_FILE)
                    .build()
            val uploadWorker =
                OneTimeWorkRequestBuilder<FileUploadWorker>().setInputData(uploadWorkData)
                    .addTag(Constants.WORKER_TAG_FILE).build()
            val referenceFileSyncWorker =
                OneTimeWorkRequestBuilder<ReferenceFileSyncWorker>().setInputData(refWorkData)
                    .build()

            // Add temporary file to parent
            val vnFile = VNFile(
                applicationContext.contentResolver.getFileName(uri)!!,
                space,
                parent,
                fileLocalId
            )
            vnFile.state = VNFile.State.UPLOADING

            fileCaches[space.id]?.addFile(vnFile)
            parent.content?.add(vnFile)

            workManager
                .beginWith(encryptionWorker)
                .then(uploadWorker)
                .then(referenceFileSyncWorker)
                .enqueue()
        }
    }

    // TODO(jatsqi): Refactor.
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

    suspend fun downloadFile(file: VNFile) {
        withContext(Dispatchers.IO) {
            val workManager = WorkManager.getInstance(applicationContext)
            val request = syncRequestService.createDownloadRequest(
                file.space.id,
                file.remoteId!!,
                file.localId!!
            )
            val downloadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to request.requestId
            )
            val refWorkData = workDataOf(
                Constants.WORKER_SPACE_ID to file.space.id
            )

            val downloadWorker =
                OneTimeWorkRequestBuilder<FileDownloadWorker>().setInputData(downloadWorkData)
                    .build()
            val referenceFileSyncWorker =
                OneTimeWorkRequestBuilder<ReferenceFileSyncWorker>().setInputData(refWorkData)
                    .build()

            workManager.beginWith(downloadWorker).then(referenceFileSyncWorker).enqueue()
        }
    }

    fun getFile(spaceId: Long, fileId: Long) = fileCaches[spaceId]?.getFile(fileId)

    suspend fun createLocalFile(
        space: VNSpace,
        parent: VNFile,
        name: String,
        fileRemoteId: Long?,
        isVirtualFolder: Boolean = false,
        initialState: VNFile.State = VNFile.State.AVAILABLE_REMOTE
    ): VNFile? {
        if (fileRemoteId != null && localFileDao.getFileByRemoteId(
                space.id,
                fileRemoteId
            ) != null
        ) {
            return null
        }

        val vnFile = VNFile(
            name,
            space,
            parent,
            localId = null,
            remoteId = fileRemoteId,
            content = if(isVirtualFolder) mutableListOf() else null
        )
        vnFile.state = initialState

        if(!isVirtualFolder) {
            val local = vnFile.mapToLocal()!!
            vnFile.localId = localFileDao.createFile(local)
        }

        val cache = fileCaches[space.id] ?: FileCache()
        cache.addFile(vnFile)
        fileCaches[space.id] = cache

        return vnFile
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

    /**
     * TODO(jatsqi): Create background worker for this.
     */
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

    suspend fun updateFileRemoteId(fileId: Long, remoteId: Long) {
        withContext(Dispatchers.IO) {
            localFileDao.updateFileRemoteId(fileId, remoteId)
        }
    }

    fun cacheEvict(spaceId: Long) {
        fileCaches.remove(spaceId)
        minimumIdCache.remove(spaceId)
    }


    private suspend fun buildTreeFromNetwork(
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

                fileCaches[space.id]?.apply {
                    addFile(folder)
                }

                buildTreeFromNetwork(it.content, localFiles, folder, space, ctx)
                parent.content!!.add(folder)
            } else if (it is NetworkFile) {
                val add = VNFile(
                    it.name,
                    space,
                    parent,
                    localId = localFiles[it.id]?.fileId,
                    remoteId = it.id
                ).apply {
                    createdAt = it.createdAt
                    lastUpdated = it.updatedAt
                }

                fileCaches[space.id]?.apply {
                    addFile(add)
                }

                if (add.isDownloaded(ctx)) {
                    add.state = VNFile.State.AVAILABLE_OFFLINE
                }
                if (!add.isDownloaded(ctx) && add.localId != null) {
                    localFileDao.deleteFiles(localFiles[add.localId]!!)
                    add.localId = null
                }

                parent.content!!.add(add)
            }
        }
    }

    /**
     * TODO(jatsqi): Remove and create background worker for this
     */
    private suspend fun resyncRefFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val cache = fileCaches[space.id]
            if (cache?.getFile(ROOT_FOLDER_ID) == null) {
                emit(ManagedResult.ConsistencyError)
                return@flow
            }

            val root = cache.getFile(ROOT_FOLDER_ID)!!.mapToNetwork() as NetworkFolder

            val referenceFile = NetworkReferenceFile(
                1,
                root.content ?: mutableListOf()
            )

            Log.e("Vault", "SYNC")
            emit(referenceFileRepository.uploadReferenceFile(referenceFile, space).first())
        }.flowOn(Dispatchers.IO)
    }

}