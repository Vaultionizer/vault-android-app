package com.vaultionizer.vaultapp.repository

import android.net.Uri
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    companion object {
        const val ROOT_FOLDER_ID = -1L
    }

    /**
     * Queries the current reference file for [space] and converts it into a fully usable
     * file tree. All files are represented using the domain model class [VNFile] which stores all
     * relevant information including additional data, which is only available at runtime like
     * the current state.
     *
     * @see [com.vaultionizer.vaultapp.data.db.entity.LocalFile]
     * @see [com.vaultionizer.vaultapp.data.model.domain.VNFile]
     *
     * @param space The space of the requested file tree.
     * @return      A single object of [VNFile] that represents the root folder of the tree.
     */
    suspend fun getFileTree(space: VNSpace): Flow<Resource<VNFile>>

    /**
     * Uploads a file to the remote server and creates an entry in the local database for this file.
     * The whole process of uploading the file is done via Android [androidx.work.WorkManager] and
     * executed in the background.
     * Furthermore an entry to the [com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest]
     * table is added which contains all information about the upload so it can be (re)started later
     * in case the upload fails or the device looses its network connection.
     * The name of the file is determined using [uri].
     *
     * **The following steps are executed in the background one after another:**
     * * The content of the file is read from [uri] and encrypted.
     * * The encrypted content is written to the local file system.
     * * The file is uploaded to the remote server.
     *
     * @see [com.vaultionizer.vaultapp.data.db.entity.LocalFile]
     *
     * @param uri       URI of the file in the local file system.
     * @param parent    Parent file, i.e, the folder the file is stored in.
     * @return          Instance of the newly created file if the background workers were started
     *                  successfully, NULL otherwise.
     */
    suspend fun uploadFile(uri: Uri, parent: VNFile): VNFile?

    /**
     * This function encrypts [data], writes it to the local file system and creates an entry in
     * the local database for the file.
     * The whole process of uploading the file is done via Android [androidx.work.WorkManager] and
     * executed in the background.
     * Furthermore an entry to the [com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest]
     * table is added which contains all information about the upload so it can be (re)started later
     * in case the upload fails or the device looses its network connection.
     *
     * @param data      The data to be uploaded.
     * @param name      The name of the file.
     * @param parent    Parent file, i.e, the folder the file is stored in.
     * @return          Instance of the newly created file if the background workers were started
     *                  successfully and the encryption was successful, NULL otherwise.
     */
    suspend fun uploadFile(data: ByteArray, name: String, parent: VNFile): VNFile?

    /**
     * Creates a "virtual" folder on the remote server.
     * "Virtual" because the server and the architecture in general is not aware of any file
     * hierarchy for security reasons. The folders and the hierarchy are stored in the encrypted
     * reference file of the space.
     * This function creates a folder entry in the reference file and syncs it back to server.
     *
     * @param name      Name of the folder.
     * @param parent    Parent file, i.e, the folder this one is stored in.
     * @return          Instance of the newly created file if the background workers were started
     *                  successfully, NULL otherwise.
     */
    suspend fun uploadFolder(name: String, parent: VNFile): VNFile?

    /**
     * Downloads a file to the remote server and writes it to the local file system.
     * The file itself is NOT decrypted during the process.
     *
     * @param file  The file to be downloaded.
     */
    suspend fun downloadFile(file: VNFile)

    /**
     * Requests the decryption of a specific file. All work is done in the background.
     * The result can be observed using the LiveData in
     * [com.vaultionizer.vaultapp.data.cache.DecryptionResultCache].
     *
     * @param file  The file to be decrypted.
     */
    suspend fun decryptFile(file: VNFile)

    /**
     * Queries a file from the local cache by its local Id.
     *
     * @param fileId    The Id of the file.
     * @return          The queried file with Id [fileId], NULL if there is no file with that Id.
     */
    suspend fun getFile(fileId: Long): VNFile?

    /**
     * Queries a file from the local cache by its remote Id.
     * The parameter [spaceId] is necessary because the user can create an arbitrary amount of
     * spaces which makes the [spaceId] ambiguous. The remote Id starts at zero for every space.
     *
     * @param spaceId       Space of the requested file.
     * @param fileRemoteId  The remote Id of the file.
     * @return              The queried file with remote Id [fileRemoteId] and space Id [spaceId],
     *                      NULL if there is no file with that Id and space.
     */
    fun getFileByRemote(spaceId: Long, fileRemoteId: Long): VNFile?

    /**
     * Announces an upcoming upload.
     *
     * @see [com.vaultionizer.vaultapp.service.FileService.uploadFile]
     *
     * @param spaceId   Id of the affected space.
     * @return          The reserved index/id for the upcoming upload.
     */
    suspend fun announceUpload(spaceId: Long): Long?

    /**
     * Deletes a file from the remote server and the entry from the local database.
     * Furthermore the file is removed from corresponding reference file of the affected space
     * and synced back to the server.
     *
     * @param file  The file to be deleted.
     */
    suspend fun deleteFile(file: VNFile)

    /**
     * Updates the remote Id of a specific file in the local database.
     *
     * @param fileId    Local Id of the affected file.
     * @param remoteId  New remote Id.
     */
    suspend fun updateFileRemoteId(fileId: Long, remoteId: Long)

    /**
     * Removes all files from the local database for a specific user.
     *
     * @param userId    Id of the user.
     */
    suspend fun clearLocalFiles(userId: Long)
}