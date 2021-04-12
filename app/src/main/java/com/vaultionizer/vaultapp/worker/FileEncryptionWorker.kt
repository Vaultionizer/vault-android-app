package com.vaultionizer.vaultapp.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vaultionizer.vaultapp.service.SyncRequestService
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.writeFileToInternal
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FileEncryptionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val syncRequestService: SyncRequestService
) : CoroutineWorker(appContext, params) {

    /**
     * TODO(johannesquast): Implement encryption
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val syncRequestId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (syncRequestId == -1L) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(syncRequestId)
            val stream =
                applicationContext.contentResolver.openInputStream(Uri.parse(request.uri!!))
                    ?: return@withContext Result.failure()

            // Write file to local file system
            writeFileToInternal(applicationContext, request.uri.toString(), stream.readBytes())

            return@withContext Result.success()
        }
    }
}