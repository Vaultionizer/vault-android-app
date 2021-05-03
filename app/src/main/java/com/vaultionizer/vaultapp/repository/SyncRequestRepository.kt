package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.data.model.domain.VNFile

interface SyncRequestRepository {

    suspend fun createDownloadRequest(localFileRef: VNFile): LocalFileSyncRequest

    suspend fun createUploadRequest(localFileRef: VNFile, data: ByteArray): LocalFileSyncRequest

    suspend fun updateRequest(request: LocalFileSyncRequest)

    suspend fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long)

    suspend fun deleteRequest(request: LocalFileSyncRequest)

    suspend fun getRequest(requestId: Long): LocalFileSyncRequest

}