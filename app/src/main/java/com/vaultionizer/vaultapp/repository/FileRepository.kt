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

    suspend fun getFileTree(space: VNSpace): Flow<Resource<VNFile>>

    suspend fun uploadFile(data: ByteArray, name: String, parent: VNFile)

    suspend fun uploadFile(parent: VNFile, uri: Uri)

    suspend fun uploadFolder(space: VNSpace, name: String, parent: VNFile)

    suspend fun downloadFile(file: VNFile)

    suspend fun decryptFile(file: VNFile)

    suspend fun getFile(fileId: Long): VNFile?

    fun getFileByRemote(spaceId: Long, fileRemoteId: Long): VNFile?

    suspend fun announceUpload(spaceId: Long): Long?

    suspend fun deleteFile(file: VNFile)

    suspend fun updateFileRemoteId(fileId: Long, remoteId: Long)

    suspend fun clearLocalFiles(userId: Long)
}