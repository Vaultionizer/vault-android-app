package com.vaultionizer.vaultapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.extension.collectSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ReferenceFileSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val referenceFileRepository: ReferenceFileRepository,
    val fileRepository: FileRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val spaceId = inputData.getLong(Constants.WORKER_SPACE_ID, -1)
            if (spaceId == -1L) {
                return@withContext Result.failure()
            }

            referenceFileRepository.syncReferenceFile(
                fileRepository.getFileByRemote(spaceId, FileRepository.ROOT_FOLDER_ID)!!
            ).collectSuccess() ?: return@withContext Result.failure()
            return@withContext Result.success()
        }
    }
}