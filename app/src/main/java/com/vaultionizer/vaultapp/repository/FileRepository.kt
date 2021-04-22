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
            val cache = fileCaches[space.id] ?: FileCache(FileCache.IdCachingStrategy.LOCAL_ID)
            fileCaches[space.id] = cache
            cache.getRootFile()?.let {
                emit(ManagedResult.Success(it))
                return@flow
            }

            val referenceFile = referenceFileRepository.downloadReferenceFile(space)
            referenceFile.collect {
                when (it) {
                    is ManagedResult.Success -> {
                        minimumIdCache[space.id] = -1
                        val affectedIds = mutableSetOf<Long>()
                        persistNetworkTree(it.data.elements, space, -1, affectedIds)

                        localFileDao.deleteAllFilesOfSpaceExceptWithIds(affectedIds, space.id)
                        val localFiles =
                            localSpaceDao.getSpaceWithFiles(space.id).files.filter { it.remoteFileId != null }
                        val root = buildLocalFileTree(space, localFiles)

                        // TODO(jatsqi):    Add files that are being uploaded to local file tree.
                        //                  Steps:
                        //                      1) Query sync requests
                        //                      2) Add new VNFile to parent folder
                        cache.addFile(root)
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
                    0,
                    space.id,
                    null,
                    parent.localId,
                    applicationContext.contentResolver.getFileName(uri) ?: "UNKNOWN",
                    LocalFile.Type.FILE,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )

            // Add temporary file to parent
            val vnFile = VNFile(
                applicationContext.contentResolver.getFileName(uri)!!,
                space,
                parent,
                fileLocalId
            )
            vnFile.state = VNFile.State.UPLOADING

            // Create upload request
            val uploadRequest =
                syncRequestService.createUploadRequest(
                    vnFile, applicationContext.contentResolver.openInputStream(
                        uri
                    )?.readBytes() ?: ByteArray(0)
                )

            val encryptionWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
            )
            val uploadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
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

            fileCaches[space.id]?.addFile(vnFile)
            parent.content?.add(vnFile)

            workManager
                .beginWith(encryptionWorker)
                .then(uploadWorker)
                .then(referenceFileSyncWorker)
                .enqueue()
        }
    }

    suspend fun uploadFolder(
        space: VNSpace,
        name: String,
        parent: VNFile
    ) {
        withContext(Dispatchers.IO) {
            if (!minimumIdCache.containsKey(space.id)) {
                minimumIdCache[space.id] = -2
            } else {
                minimumIdCache[space.id] = minimumIdCache[space.id]!! - 1
            }

            val localFileId = localFileDao.createFile(
                LocalFile(
                    0,
                    space.id,
                    minimumIdCache[space.id]!!,
                    parent.localId,
                    name,
                    LocalFile.Type.FOLDER,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )

            val folder = VNFile(
                name,
                space,
                parent,
                localFileId,
                minimumIdCache[space.id]!!,
                mutableListOf()
            )

            fileCaches[space.id]?.addFile(folder)
            parent.content?.add(folder)

            val refWorkData = workDataOf(
                Constants.WORKER_SPACE_ID to space.id
            )
            val refWorker =
                OneTimeWorkRequestBuilder<ReferenceFileSyncWorker>().setInputData(refWorkData)
                    .build()

            WorkManager.getInstance(applicationContext).enqueue(refWorker)
        }
    }

    suspend fun downloadFile(file: VNFile) {
        withContext(Dispatchers.IO) {
            val workManager = WorkManager.getInstance(applicationContext)
            val request = syncRequestService.createDownloadRequest(file)

            val downloadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to request.requestId
            )
            val refWorkData = workDataOf(
                Constants.WORKER_SPACE_ID to file.space.id
            )

            val downloadWorker =
                OneTimeWorkRequestBuilder<FileDownloadWorker>().setInputData(downloadWorkData)
                    .addTag(Constants.WORKER_TAG_FILE)
                    .build()
            val referenceFileSyncWorker =
                OneTimeWorkRequestBuilder<ReferenceFileSyncWorker>().setInputData(refWorkData)
                    .build()

            workManager.beginWith(downloadWorker).then(referenceFileSyncWorker).enqueue()
        }
    }

    suspend fun getFile(fileId: Long): VNFile? {
        fileCaches.values.forEach {
            val file = it.getFileByStrategy(fileId, FileCache.IdCachingStrategy.LOCAL_ID)
            if (file != null) {
                return file
            }
        }

        val localFile = localFileDao.getFileById(fileId)
        if (localFile != null) {
            // TODO(jatsqi): Return file if not cached.
        }
        return null
    }

    fun getFileByRemote(spaceId: Long, fileRemoteId: Long): VNFile? =
        fileCaches[spaceId]?.getFile(fileRemoteId)

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

    private suspend fun persistNetworkTree(
        elements: List<NetworkElement>?,
        space: VNSpace,
        parentId: Long,
        affectedLocalFiles: MutableSet<Long>
    ) {
        elements?.forEach {
            val file = localFileDao.getFileByRemoteId(space.id, it.id)
            if (file != null) {
                file.parentFileId = parentId
                affectedLocalFiles.add(file.fileId)
                if (it is NetworkFolder) {
                    persistNetworkTree(
                        it.content ?: listOf(),
                        space,
                        file.fileId,
                        affectedLocalFiles
                    )
                }
            } else {
                val type = if (it is NetworkFolder) {
                    LocalFile.Type.FOLDER
                } else {
                    LocalFile.Type.FILE
                }

                val localFile = LocalFile(
                    0,
                    space.id,
                    it.id,
                    parentId,
                    it.name,
                    type,
                    // TODO(jatsqi): Set correct timestamps
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )

                affectedLocalFiles.add(localFileDao.createFile(localFile))
            }
        }
    }

    private fun buildLocalFileTree(space: VNSpace, elements: List<LocalFile>): VNFile {
        val files = mutableMapOf<Long, VNFile>()
        val flatChildrenTree = mutableMapOf<Long, MutableList<VNFile>>()

        // Init root folder
        flatChildrenTree[-1] = mutableListOf()
        files[-1] = VNFile(
            "/",
            space,
            null,
            -1,
            null,
            flatChildrenTree[-1]
        )

        elements.sortedBy {
            it.parentFileId
        }.forEach {
            val parentId = it.parentFileId
            val children: MutableList<VNFile>? =
                if (it.type == LocalFile.Type.FOLDER) {
                    flatChildrenTree[it.fileId] ?: mutableListOf()
                } else {
                    null
                }
            val childrenOfParent = flatChildrenTree[parentId] ?: mutableListOf()

            if (it.remoteFileId != null && minimumIdCache.containsKey(space.id)) {
                if (minimumIdCache[space.id]!! > it.remoteFileId) {
                    minimumIdCache[space.id] = it.remoteFileId
                }
            }

            val vnFile = VNFile(
                it.name,
                space,
                files[parentId],
                it.fileId,
                it.remoteFileId,
                children
            ).apply {
                createdAt = it.createdAt
                lastUpdated = it.lastUpdated
                lastSyncTimestamp = it.lastSyncTimestamp
            }

            fileCaches[space.id]?.addFile(vnFile)

            if (children != null) {
                flatChildrenTree[it.fileId] = children
            }
            childrenOfParent.add(vnFile)
            files[it.fileId] = vnFile
            flatChildrenTree[parentId] = childrenOfParent
        }

        return files[-1]!!
    }

    /**
     * TODO(jatsqi): Remove and create background worker for this
     */
    private suspend fun resyncRefFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>> {
        return flow {
            val cache = fileCaches[space.id]
            if (cache?.getRootFile() == null) {
                emit(ManagedResult.ConsistencyError)
                return@flow
            }

            val root = cache.getRootFile()!!.mapToNetwork() as NetworkFolder

            val referenceFile = NetworkReferenceFile(
                1,
                root.content ?: mutableListOf()
            )

            emit(referenceFileRepository.uploadReferenceFile(referenceFile, space).first())
        }.flowOn(Dispatchers.IO)
    }

}