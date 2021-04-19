package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadFileRequest
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.FileService
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FileDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val syncRequestService: SyncRequestService,
    val fileExchangeService: FileExchangeService,
    val spaceRepository: SpaceRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val requestId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (requestId == -1L) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(requestId)
            // TODO(jatsqi): Null handling
            fileExchangeService.downloadFile(spaceRepository.getSpaceRemoteId(request.spaceId)!!, request.remoteFileId!!)
            return@withContext Result.success()
        }
    }
}