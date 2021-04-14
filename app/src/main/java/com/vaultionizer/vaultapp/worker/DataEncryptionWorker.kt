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
class DataEncryptionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val syncRequestService: SyncRequestService
) : CoroutineWorker(appContext, params) {

    /**
     * TODO(johannesquast): Implement encryption
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val spaceId = inputData.getLong(Constants.WORKER_SPACE_ID, -1)
            val inputBytes = inputData.getByteArray(Constants.WORKER_FILE_BYTES)
            if (spaceId == -1L || inputBytes == null) {
                return@withContext Result.failure()
            }

            // TODO(jatsqi): Encrypt [inputBytes]
            return@withContext Result.success(workDataOf(Constants.WORKER_FILE_BYTES to inputBytes))
        }
    }
}