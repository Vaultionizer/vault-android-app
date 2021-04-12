package com.vaultionizer.vaultapp.service

import android.net.Uri
import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.repository.AuthRepository
import javax.inject.Inject

class SyncRequestService @Inject constructor(val localFileSyncRequestDao: LocalFileSyncRequestDao) {

    fun createDownloadRequest(spaceId: Long): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.DOWNLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            null,
            null
        )

        localFileSyncRequestDao.createRequest(request)
        return request
    }

    fun createUploadRequest(spaceId: Long, localUri: Uri): LocalFileSyncRequest {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.UPLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            localUri.path,
            null
        )

        localFileSyncRequestDao.createRequest(request)
        return request
    }

    fun updateRequest(request: LocalFileSyncRequest) {
        localFileSyncRequestDao.updateRequest(request)
    }

    fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long) {
        localFileSyncRequestDao.updateRemoteFileId(requestId, newRemoteFileId)
    }

    fun getRequest(requestId: Long): LocalFileSyncRequest {
        return localFileSyncRequestDao.getById(requestId)
    }

}