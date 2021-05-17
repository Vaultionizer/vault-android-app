package com.vaultionizer.vaultapp.repository.impl

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.cache.FileCache
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.repository.SyncRequestRepository
import com.vaultionizer.vaultapp.service.FileService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.extension.collectSuccess
import com.vaultionizer.vaultapp.util.getFileName
import com.vaultionizer.vaultapp.worker.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    val gson: Gson,
    val referenceFileRepository: ReferenceFileRepository,
    val spaceRepository: SpaceRepository,
    val localFileDao: LocalFileDao,
    val localSpaceDao: LocalSpaceDao,
    val fileService: FileService,
    val syncRequestService: SyncRequestRepository,
) : FileRepository {

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

    override suspend fun getFileTree(space: VNSpace): Flow<Resource<VNFile>> {
        return flow {
            if (!space.isKeyAvailable) {
                emit(Resource.CryptographicalError)
                return@flow
            }

            val cache = fileCaches[space.id] ?: FileCache(FileCache.IdCachingStrategy.LOCAL_ID)
            fileCaches[space.id] = cache
            cache.rootFile?.let {
                emit(Resource.Success(it))
                return@flow
            }

            referenceFileRepository.downloadReferenceFile(space).collect {
                if (it is Resource.Loading) {
                    return@collect
                }

                when (it) {
                    is Resource.Success -> {
                        val referenceFile = it.data
                        minimumIdCache[space.id] = -1
                        val affectedIds = mutableSetOf<Long>()
                        persistNetworkTree(referenceFile.elements, space, -1, affectedIds)

                        localFileDao.deleteAllFilesOfSpaceExceptWithIds(affectedIds, space.id)
                        val localFiles =
                            localSpaceDao.getSpaceWithFiles(space.id).files.filter { it.remoteFileId != null }
                        val root = buildLocalFileTree(space, localFiles)

                        // TODO(jatsqi):    Add files that are being uploaded to local file tree.
                        //                  Steps:
                        //                      1) Query sync requests
                        //                      2) Add new VNFile to parent folder
                        cache.addFile(root)
                        emit(Resource.Success(root))

                        fileCaches[space.id] = cache
                        return@collect
                    }
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        val convertedError = it as? Resource<VNFile>

                        if (convertedError == null) {
                            emit(Resource.UnknownError)
                        } else emit(convertedError)
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun uploadFile(
        data: ByteArray,
        name: String,
        parent: VNFile,
    ) {
        withContext(Dispatchers.IO) {
            val name = resolveFileNameConflicts(parent, name)

            // Create file in DB
            val fileLocalId = localFileDao.createFile(
                LocalFile(
                    0,
                    parent.space.id,
                    null,
                    parent.localId,
                    name,
                    LocalFile.Type.FILE,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )

            // Add temporary file to parent
            val vnFile = VNFile(
                name,
                parent.space,
                parent,
                fileLocalId
            )
            vnFile.state = VNFile.State.UPLOADING

            // Create upload request
            val uploadRequest =
                syncRequestService.createUploadRequest(vnFile, data)

            val encryptionWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
            )
            val uploadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to uploadRequest.requestId,
            )

            val encryptionWorker =
                prepareFileWorkerBuilder<DataEncryptionWorker>(vnFile, encryptionWorkData)
                    .addTag(Constants.WORKER_TAG_FILE)
                    .addTag(Constants.WORKER_TAG_ENCRYPTION)
                    .build()
            val uploadWorker =
                prepareFileWorkerBuilder<FileUploadWorker>(vnFile, uploadWorkData)
                    .addTag(Constants.WORKER_TAG_FILE)
                    .build()

            fileCaches[parent.space.id]?.addFile(vnFile)
            parent.content!!.add(vnFile)

            enqueueUniqueFileWork(
                vnFile,
                encryptionWorker,
                uploadWorker,
                buildReferenceFileWorker(vnFile)
            )
        }
    }

    override suspend fun uploadFile(parent: VNFile, uri: Uri) {
        // TODO(jatsqi): Uri error handling
        uploadFile(
            applicationContext.contentResolver.openInputStream(uri)!!.readBytes(),
            applicationContext.contentResolver.getFileName(uri) ?: "UNKNOWN",
            parent
        )
    }

    override suspend fun uploadFolder(
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

            val name = resolveFileNameConflicts(parent, name)

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

            enqueueUniqueFileWork(folder, buildReferenceFileWorker(folder))
        }
    }

    override suspend fun downloadFile(file: VNFile) {
        withContext(Dispatchers.IO) {
            val request = syncRequestService.createDownloadRequest(file)

            val downloadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to request.requestId
            )

            file.state = VNFile.State.DOWNLOADING

            val downloadWorker =
                prepareFileWorkerBuilder<FileDownloadWorker>(file, downloadWorkData)
                    .addTag(Constants.WORKER_TAG_FILE)
                    .build()

            enqueueUniqueFileWork(file, downloadWorker)
        }
    }

    override suspend fun decryptFile(file: VNFile) {
        val decryptionWorkData = workDataOf(
            Constants.WORKER_FILE_ID to file.localId
        )
        val decryptionWorker =
            prepareFileWorkerBuilder<DataDecryptionWorker>(file, decryptionWorkData)
                .addTag(Constants.WORKER_TAG_FILE)
                .addTag(Constants.WORKER_TAG_DECRYPTION)
                .build()

        enqueueUniqueFileWork(file, decryptionWorker)
    }

    override suspend fun getFile(fileId: Long): VNFile? {
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

    override fun getFileByRemote(spaceId: Long, fileRemoteId: Long): VNFile? =
        fileCaches[spaceId]?.getFile(fileRemoteId)

    override suspend fun announceUpload(spaceId: Long): Long? {
        val space = spaceRepository.getSpace(spaceId).collectSuccess() ?: return null

        val saveIndex = fileService.uploadFile(
            UploadFileRequest(1),
            space.remoteId
        )

        if (saveIndex !is ApiResult.Success) {
            return null
        }

        return saveIndex.data
    }

    override suspend fun deleteFile(file: VNFile) {
        localFileDao.deleteFile(file.localId)
        fileCaches[file.space.id]?.deleteFile(file)

        if (file.parent != null) {
            file.parent.content?.remove(file)
        }

        enqueueUniqueFileWork(file, buildReferenceFileWorker(file))
    }

    override suspend fun updateFileRemoteId(fileId: Long, remoteId: Long) {
        withContext(Dispatchers.IO) {
            localFileDao.updateFileRemoteId(fileId, remoteId)
            getFile(fileId)?.remoteId = remoteId
        }
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

            if (it.remoteFileId != null
                && minimumIdCache.containsKey(space.id)
                && minimumIdCache[space.id]!! > it.remoteFileId
            ) {
                minimumIdCache[space.id] = it.remoteFileId
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
            if (vnFile.isDownloaded(applicationContext)) {
                vnFile.state = VNFile.State.AVAILABLE_OFFLINE
            }

            if (children != null) {
                flatChildrenTree[it.fileId] = children
            }
            childrenOfParent.add(vnFile)
            files[it.fileId] = vnFile
            flatChildrenTree[parentId] = childrenOfParent
        }

        return files[-1]!!
    }

    private fun resolveFileNameConflicts(parent: VNFile, name: String): String {
        val nameSet = parent.content?.map { it.name }?.toSet() ?: return name

        nameSet.forEach {
            if (it == name) {
                var currentIndex = 1
                while (nameSet.contains(buildDuplicateFileName(name, currentIndex)))
                    ++currentIndex

                return buildDuplicateFileName(name, currentIndex)
            }
        }

        return name
    }

    private fun buildDuplicateFileName(name: String, index: Int) = "($index) $name"

    private fun buildReferenceFileWorker(file: VNFile) =
        prepareFileWorkerBuilder<ReferenceFileSyncWorker>(
            file,
            workDataOf(
                Constants.WORKER_SPACE_ID to file.space.id
            )
        ).addTag(Constants.WORKER_TAG_REFERENCE_FILE).build()

    private fun buildDefaultNetworkConstraints() =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    private fun enqueueUniqueFileWork(file: VNFile, vararg workers: OneTimeWorkRequest) {
        var chain = WorkManager.getInstance(applicationContext).beginUniqueWork(
            String.format(Constants.WORKER_FILE_UNIQUE_NAME_TEMPLATE, file.localId),
            ExistingWorkPolicy.KEEP,
            workers[0]
        )

        for (i in 1 until workers.size) {
            chain = chain.then(workers[i])
        }

        chain.enqueue()
    }

    private inline fun <reified W : ListenableWorker> prepareFileWorkerBuilder(
        file: VNFile,
        inputData: Data
    ): OneTimeWorkRequest.Builder = OneTimeWorkRequestBuilder<W>()
        .setInputData(inputData)
        .setConstraints(buildDefaultNetworkConstraints())
        .addTag(String.format(Constants.WORKER_TAG_FILE_ID_TEMPLATE, file.localId))

    override suspend fun clearLocalFiles(userId: Long) {
        localFileDao.deleteAllFilesOfUser(userId)
    }
}