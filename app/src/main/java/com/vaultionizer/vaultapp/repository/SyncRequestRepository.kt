package com.vaultionizer.vaultapp.repository

import android.net.Uri
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.data.model.domain.VNFile

interface SyncRequestRepository {

    suspend fun createDownloadRequest(localFileRef: VNFile): LocalFileSyncRequest

    suspend fun createUploadRequest(localFileRef: VNFile, uri: Uri): LocalFileSyncRequest

    suspend fun updateRequest(request: LocalFileSyncRequest)

    suspend fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long)

    suspend fun deleteRequest(request: LocalFileSyncRequest)

    suspend fun getRequest(requestId: Long): LocalFileSyncRequest

}