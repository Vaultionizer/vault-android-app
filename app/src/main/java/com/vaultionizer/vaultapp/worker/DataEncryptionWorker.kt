package com.vaultionizer.vaultapp.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SyncRequestRepository
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.getAbsoluteFilePath
import com.vaultionizer.vaultapp.util.writeFile
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

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val requestId = inputData.getLong(Constants.WORKER_SYNC_REQUEST_ID, -1)
            if (requestId == -1L) {
                return@withContext Result.failure()
            }

            val request = syncRequestService.getRequest(requestId)
            if (request.cryptographicOperationDone) {
                return@withContext Result.success()
            }

            val file =
                fileRepository.getFile(request.localFileId) ?: return@withContext Result.failure()
            val uri = Uri.parse(request.uri ?: return@withContext Result.failure())
            val bytes = applicationContext.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@withContext Result.failure()

            try {
                val encryptedBytes = CryptoUtils.encryptData(
                    file.space.id, bytes
                )

                applicationContext.writeFile(file.localId, encryptedBytes)

                request.cryptographicOperationDone = true
                request.uri = applicationContext.getAbsoluteFilePath(file.localId).toString()
                syncRequestService.updateRequest(request)
            } catch (e: RuntimeException) {
                return@withContext Result.failure()
            }

            return@withContext Result.success()
        }
    }
}