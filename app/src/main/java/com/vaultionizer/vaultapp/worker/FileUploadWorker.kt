package com.vaultionizer.vaultapp.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.repository.SyncRequestRepository
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.writeFile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    val syncRequestService: SyncRequestRepository,
    val fileExchangeService: FileExchangeService,
    val spaceRepository: SpaceRepository,
    val fileRepository: FileRepository
) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val requestId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (requestId == -1L) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(requestId)
            val file =
                fileRepository.getFile(request.localFileId) ?: return@withContext Result.failure()
            val uri = Uri.parse(request.uri ?: return@withContext Result.failure())

            if (request.remoteFileId == null) {
                val fileRemoteId =
                    fileRepository.announceUpload(file.space.id)
                        ?: return@withContext Result.failure()

                request.remoteFileId = fileRemoteId
                syncRequestService.updateRequest(request)

                fileRepository.updateFileRemoteId(file.localId, fileRemoteId)
            }

            val bytes = applicationContext.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@withContext Result.failure()
            try {
                fileExchangeService.uploadFile(
                        fileRepository.getFile(request.localFileId)?.space!!.remoteId,
                        request.remoteFileId!!,
                        bytes
                )
                file.lastUpdated = System.currentTimeMillis()
            } catch (exception: Exception) {
                return@withContext Result.failure()
            }

            // Write file to local file system
            applicationContext.writeFile(file.localId, bytes)

            val vnFile = fileRepository.getFile(request.localFileId)
            vnFile?.remoteId = request.remoteFileId
            vnFile?.state = VNFile.State.AVAILABLE_OFFLINE

            syncRequestService.deleteRequest(request)
            fileRepository.updateFileRemoteId(request.localFileId, request.remoteFileId!!)
            return@withContext Result.success()
        }
    }
}