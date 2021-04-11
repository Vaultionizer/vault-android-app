package com.vaultionizer.vaultapp.service

import android.net.Uri
import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.repository.AuthRepository
import javax.inject.Inject

class SyncRequestService @Inject constructor(val localFileSyncRequestDao: LocalFileSyncRequestDao) {

    fun createDownloadRequest(spaceId: Long) {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.DOWNLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            null,
            null
        )

        localFileSyncRequestDao.createRequest(request)
    }

    fun createUploadRequest(spaceId: Long, localUri: Uri) {
        val request = LocalFileSyncRequest(
            0,
            LocalFileSyncRequest.Type.UPLOAD,
            AuthRepository.user!!.localUser.userId,
            spaceId,
            localUri.path,
            null
        )

        localFileSyncRequestDao.createRequest(request)
    }

    fun updateRemoteFileId(requestId: Long, newRemoteFileId: Long) {
        localFileSyncRequestDao.updateRemoteFileId(requestId, newRemoteFileId)
    }

}