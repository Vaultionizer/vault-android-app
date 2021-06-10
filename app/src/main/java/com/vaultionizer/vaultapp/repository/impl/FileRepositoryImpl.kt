package com.vaultionizer.vaultapp.repository.impl

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.gson.Gson
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
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
import com.vaultionizer.vaultapp.util.*
import com.vaultionizer.vaultapp.util.extension.collectSuccess
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

    private sealed class FilePushMode(val affectedFile: VNFile) {
        data class Upload(val parent: VNFile) : FilePushMode(parent)
        data class Update(val file: VNFile) : FilePushMode(file)
    }

    private sealed class FilePushDataSource {
        data class InMemory(val data: ByteArray, val fileName: String) : FilePushDataSource()
        data class LocalFileSystem(val uri: Uri) : FilePushDataSource()
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
        uri: Uri,
        parent: VNFile,
    ): VNFile? = pushFile(FilePushMode.Upload(parent), FilePushDataSource.LocalFileSystem(uri))

    override suspend fun uploadFile(data: ByteArray, name: String, parent: VNFile): VNFile? =
        pushFile(
            FilePushMode.Upload(
                parent
            ), FilePushDataSource.InMemory(data, name)
        )

    override suspend fun uploadFolder(
        name: String,
        parent: VNFile
    ): VNFile? {
        return withContext(Dispatchers.IO) {
            val space = parent.space
            if (!minimumIdCache.containsKey(space.id)) {
                minimumIdCache[space.id] = -2
            } else {
                minimumIdCache[space.id] = minimumIdCache[space.id]!! - 1
            }

            @Suppress("NAME_SHADOWING") val name = resolveFileNameConflicts(parent, name)

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

            enqueueUniqueFileWork(applicationContext, folder, buildReferenceFileWorker(folder))

            return@withContext folder
        }
    }

    override suspend fun updateFile(file: VNFile, uri: Uri): Boolean = pushFile(
        FilePushMode.Update(file), FilePushDataSource.LocalFileSystem(uri)
    ) != null

    override suspend fun updateFile(file: VNFile, data: ByteArray) = pushFile(
        FilePushMode.Update(file), FilePushDataSource.InMemory(data, file.name)
    ) != null

    override suspend fun downloadFile(file: VNFile) {
        withContext(Dispatchers.IO) {
            val request = syncRequestService.createDownloadRequest(file)

            val downloadWorkData = workDataOf(
                Constants.WORKER_SYNC_REQUEST_ID to request.requestId
            )

            file.state = VNFile.State.DOWNLOADING

            val downloadWorker =
                prepareFileWorkerBuilder<FileDownloadWorker>(file, downloadWorkData)
                    .addTag(Constants.WORKER_TAG_DOWNLOAD)
                    .build()

            enqueueUniqueFileWork(applicationContext, file, downloadWorker)
        }
    }

    override suspend fun decryptFile(file: VNFile) {
        val decryptionWorkData = workDataOf(
            Constants.WORKER_FILE_ID to file.localId
        )
        val decryptionWorker =
            prepareFileWorkerBuilder<DataDecryptionWorker>(file, decryptionWorkData)
                .addTag(Constants.WORKER_TAG_DECRYPTION)
                .build()

        enqueueUniqueFileWork(applicationContext, file, decryptionWorker)
    }

    override suspend fun getFile(fileId: Long): VNFile? {
        for (cache in fileCaches.values) {
            val file = cache.getFileByStrategy(fileId, FileCache.IdCachingStrategy.LOCAL_ID)
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

        enqueueUniqueFileWork(applicationContext, file, buildReferenceFileWorker(file))
    }

    override suspend fun updateFileRemoteId(fileId: Long, remoteId: Long) {
        withContext(Dispatchers.IO) {
            localFileDao.updateFileRemoteId(fileId, remoteId)
            getFile(fileId)?.remoteId = remoteId
        }
    }

    override suspend fun clearLocalFiles(userId: Long) {
        localFileDao.deleteAllFilesOfUser(userId)
    }

    private suspend fun pushFile(
        filePushMode: FilePushMode,
        filePushDataSource: FilePushDataSource
    ): VNFile? {
        return withContext(Dispatchers.IO) {
            if (!filePushMode.affectedFile.space.isKeyAvailable) {
                return@withContext null
            }

            // Determine final file name.
            val name: String = when (filePushDataSource) {
                is FilePushDataSource.LocalFileSystem -> {
                    applicationContext.contentResolver.getFileName(filePushDataSource.uri)
                        ?: "Unknown"
                }
                is FilePushDataSource.InMemory -> {
                    filePushDataSource.fileName
                }
            }

            // Construct domain model of file.
            val vnFile: VNFile = when (filePushMode) {
                is FilePushMode.Upload -> {
                    val file = createUnsynchronizedFile(
                        resolveFileNameConflicts(
                            filePushMode.affectedFile,
                            name
                        ),
                        filePushMode.affectedFile
                    )

                    fileCaches[filePushMode.parent.space.id]?.addFile(file)
                    filePushMode.parent.content!!.add(file)
                    file
                }

                is FilePushMode.Update -> {
                    val file = filePushMode.file
                    file.lastUpdated = System.currentTimeMillis()
                    file
                }
            }

            // Get URI of file and encrypt if necessary.
            val uri: Uri = when (filePushDataSource) {
                is FilePushDataSource.InMemory -> {
                    try {
                        val encryptedData =
                            tryEncryptData(filePushMode.affectedFile.space, filePushDataSource.data)
                                ?: return@withContext null
                        applicationContext.writeFile(
                            vnFile.localId,
                            encryptedData
                        )
                    } catch (ex: Exception) {
                        return@withContext null
                    }

                    applicationContext.getAbsoluteFilePath(vnFile.localId)
                }

                is FilePushDataSource.LocalFileSystem -> {
                    filePushDataSource.uri
                }
            }

            val syncRequest = syncRequestService.createUploadRequest(vnFile, uri)
            if (filePushDataSource is FilePushDataSource.InMemory) {
                syncRequest.cryptographicOperationDone = true
                syncRequestService.updateRequest(syncRequest)
            }

            enqueueUniqueFileWork(
                applicationContext,
                vnFile,
                buildEncryptionWorker(vnFile, syncRequest.requestId),
                buildUploadWorker(vnFile, syncRequest.requestId),
                buildReferenceFileWorker(vnFile)
            )
            return@withContext vnFile
        }
    }

    private suspend fun tryEncryptData(space: VNSpace, data: ByteArray): ByteArray? {
        return withContext(Dispatchers.IO) {
            var encryptedBytes: ByteArray?

            try {
                encryptedBytes = CryptoUtils.encryptData(space.id, data)
            } catch (ex: Exception) {
                return@withContext null
            }

            return@withContext encryptedBytes
        }
    }

    private suspend fun createUnsynchronizedFile(name: String, parent: VNFile): VNFile {
        val localFile = LocalFile(
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
        val localFileId = localFileDao.createFile(localFile)
        return VNFile(
            name, parent.space, parent, localFileId, null, null
        )
    }

    private suspend fun persistNetworkTree(
        elements: List<NetworkElement>?,
        space: VNSpace,
        parentId: Long,
        affectedLocalFiles: MutableSet<Long>
    ) {
        for (element in elements ?: emptyList()) {
            val file = localFileDao.getFileByRemoteId(space.id, element.id)
            if (file != null) {
                file.parentFileId = parentId
                affectedLocalFiles.add(file.fileId)
                if (element is NetworkFolder) {
                    persistNetworkTree(
                        element.content ?: listOf(),
                        space,
                        file.fileId,
                        affectedLocalFiles
                    )
                }
            } else {
                val type = if (element is NetworkFolder) {
                    LocalFile.Type.FOLDER
                } else {
                    LocalFile.Type.FILE
                }

                val localFile = LocalFile(
                    0,
                    space.id,
                    element.id,
                    parentId,
                    element.name,
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

        val sortedElements = elements.sortedBy {
            it.parentFileId
        }
        for (element in sortedElements) {
            val parentId = element.parentFileId
            val children: MutableList<VNFile>? =
                if (element.type == LocalFile.Type.FOLDER) {
                    flatChildrenTree[element.fileId] ?: mutableListOf()
                } else {
                    null
                }
            val childrenOfParent = flatChildrenTree[parentId] ?: mutableListOf()

            if (element.remoteFileId != null
                && minimumIdCache.containsKey(space.id)
                && minimumIdCache[space.id]!! > element.remoteFileId
            ) {
                minimumIdCache[space.id] = element.remoteFileId
            }

            val vnFile = VNFile(
                element.name,
                space,
                files[parentId],
                element.fileId,
                element.remoteFileId,
                children
            ).apply {
                createdAt = element.createdAt
                lastUpdated = element.lastUpdated
                lastSyncTimestamp = element.lastSyncTimestamp
            }

            fileCaches[space.id]?.addFile(vnFile)
            if (vnFile.isDownloaded(applicationContext)) {
                vnFile.state = VNFile.State.AVAILABLE_OFFLINE
            }

            if (children != null) {
                flatChildrenTree[element.fileId] = children
            }
            childrenOfParent.add(vnFile)
            files[element.fileId] = vnFile
            flatChildrenTree[parentId] = childrenOfParent
        }

        return files[-1]!!
    }
}