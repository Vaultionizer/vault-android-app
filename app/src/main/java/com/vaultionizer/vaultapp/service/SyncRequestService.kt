package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import javax.inject.Inject

class SyncRequestService @Inject constructor(val localFileSyncRequestDao: LocalFileSyncRequestDao) {

    suspend fun createDownloadRequest(localFileRef: VNFile): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.DOWNLOAD,
            localFileRef.localId,
            false,
            localFileRef.remoteId
        )

        request.requestId = localFileSyncRequestDao.createRequest(request)
        return request
    }

    suspend fun createUploadRequest(localFileRef: VNFile, data: ByteArray): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.UPLOAD,
            localFileRef.localId,
            false,
            null,
            data
        )

        request.requestId = localFileSyncRequestDao.createRequest(request)
        return request
    }

    suspend fun updateRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.updateRequest(request)
    }

    suspend fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long) {
        localFileSyncRequestDao.updateRemoteFileId(requestId, newRemoteFileId)
    }

    suspend fun deleteRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.deleteRequest(request)
    }

    suspend fun getRequest(requestId: Long): LocalFileSyncRequest {
        return localFileSyncRequestDao.getById(requestId)
    }

}