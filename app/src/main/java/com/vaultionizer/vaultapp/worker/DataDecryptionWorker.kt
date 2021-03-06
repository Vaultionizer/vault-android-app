package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.readFile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

@HiltWorker
class DataDecryptionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val decryptionResultCache: DecryptionResultCache,
    val fileRepository: FileRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val fileId = inputData.getLong(Constants.WORKER_FILE_ID, -1)
            if (fileId == -1L) {
                return@withContext Result.failure()
            }

            try {
                val file = fileRepository.getFile(fileId) ?: return@withContext Result.failure()
                val bytes = applicationContext.readFile(fileId)

                decryptionResultCache.addDecryptionResult(
                    file,
                    CryptoUtils.decryptData(file.space.id, bytes)
                )
            } catch (ex: FileNotFoundException) {
                return@withContext Result.failure()
            }

            return@withContext Result.success()
        }
    }
}