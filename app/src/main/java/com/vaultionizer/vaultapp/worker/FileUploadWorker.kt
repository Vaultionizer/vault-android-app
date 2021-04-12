package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.lang.Exception

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    val syncRequestService: SyncRequestService,
    val fileExchangeService: FileExchangeService,
    val spaceRepository: SpaceRepository,
    val fileRepository: FileRepository
) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val fileBytes = inputData.getByteArray(Constants.WORKER_FILE_BYTES)
            val requestId = inputData.getLong(Constants.WORKER_SPACE_ID, -1)
            if (requestId == -1L || fileBytes == null) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(requestId)
            val space = spaceRepository.getSpace(request.spaceId).first()

            if(space !is ManagedResult.Success) {
                return@withContext Result.failure()
            }

            if(request.remoteFileId == null) {
                val announceResponse =
                    fileRepository.announceUpload(space.data.remoteId).first()

                if (announceResponse !is ManagedResult.Success) {
                    return@withContext Result.failure()
                }

                request.remoteFileId = announceResponse.data
                syncRequestService.updateRequest(request)
            }

            try {
                fileExchangeService.uploadFile(space.data.remoteId, request.remoteFileId!!, fileBytes)
            } catch (exception: Exception) {
                return@withContext Result.failure()
            }

            syncRequestService.deleteRequest(request)
            return@withContext Result.success()
        }
    }
}