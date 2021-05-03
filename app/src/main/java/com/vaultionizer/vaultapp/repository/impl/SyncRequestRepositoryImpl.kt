package com.vaultionizer.vaultapp.repository.impl

import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.repository.SyncRequestRepository
import javax.inject.Inject

class SyncRequestRepositoryImpl @Inject constructor(val localFileSyncRequestDao: LocalFileSyncRequestDao) :
    SyncRequestRepository {

    override suspend fun createDownloadRequest(localFileRef: VNFile): LocalFileSyncRequest {
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

    override suspend fun createUploadRequest(
        localFileRef: VNFile,
        data: ByteArray
    ): LocalFileSyncRequest {
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

    override suspend fun updateRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.updateRequest(request)
    }

    override suspend fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long) {
        localFileSyncRequestDao.updateRemoteFileId(requestId, newRemoteFileId)
    }

    override suspend fun deleteRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.deleteRequest(request)
    }

    override suspend fun getRequest(requestId: Long): LocalFileSyncRequest {
        return localFileSyncRequestDao.getById(requestId)
    }

}