package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    val syncRequestService: SyncRequestService,
    val fileExchangeService: FileExchangeService
) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val syncRequestId =
                inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (syncRequestId == -1L)
                return@withContext Result.failure()

            val syncRequest = syncRequestService.getRequest(syncRequestId)
            if (syncRequest.remoteFileId != null) {

            }

            return@withContext Result.success()
        }
    }
}