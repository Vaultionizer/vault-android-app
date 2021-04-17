package com.vaultionizer.vaultapp.service

import android.net.Uri
import android.util.Log
import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.repository.AuthRepository
import javax.inject.Inject

class SyncRequestService @Inject constructor(val localFileSyncRequestDao: LocalFileSyncRequestDao) {

    fun createDownloadRequest(spaceId: Long, remoteFileId: Long, localFileId: Long): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.DOWNLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            null,
            remoteFileId,
            localFileId,
            null
        )

        request.requestId = localFileSyncRequestDao.createRequest(request)
        return request
    }

    fun createUploadRequest(spaceId: Long, localUri: Uri, localFileId: Long, parentFileId: Long): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.UPLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            localUri.toString(),
            null,
            localFileId,
            parentFileId
        )

        request.requestId = localFileSyncRequestDao.createRequest(request)
        Log.d("Vault", "ROWID ${request.requestId}")
        return request
    }

    fun updateRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.updateRequest(request)
    }

    fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long) {
        localFileSyncRequestDao.updateRemoteFileId(requestId, newRemoteFileId)
    }

    fun deleteRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.deleteRequest(request)
    }

    fun getRequest(requestId: Long): LocalFileSyncRequest {
        return localFileSyncRequestDao.getById(requestId)
    }

}