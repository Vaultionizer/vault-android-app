package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.cryptography.Cryptography
import com.vaultionizer.vaultapp.repository.FileRepository
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
    val syncRequestService: SyncRequestRepository,
    val fileRepository: FileRepository
) : CoroutineWorker(appContext, params) {

    /**
     * TODO(johannesquast): Implement encryption
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val requestId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (requestId == -1L) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(requestId)
            val file = fileRepository.getFile(request.localFileId) ?: return@withContext Result.failure()

            try {
                val encryptedBytes = Cryptography().encryptorNoPadder(file.space.id, request.data!!)
                request.data = encryptedBytes
                request.cryptographicOperationDone = true
                syncRequestService.updateRequest(request)
            } catch (e : RuntimeException) {
                return@withContext Result.failure()
            }

            return@withContext Result.success()
        }
    }
}