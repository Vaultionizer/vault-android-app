package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.repository.SyncRequestRepository
import com.vaultionizer.vaultapp.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DataEncryptionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val syncRequestService: SyncRequestRepository
) : CoroutineWorker(appContext, params) {

    /**
     * TODO(johannesquast): Implement encryption
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val spaceId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (spaceId == -1L) {
                return@withContext Result.failure()
            }

            // TODO(jatsqi): Encrypt bytes stored in request.
            return@withContext Result.success()
        }
    }
}