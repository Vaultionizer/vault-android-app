package com.vaultionizer.vaultapp.util

import android.content.Context
import androidx.work.*
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.worker.DataEncryptionWorker
import com.vaultionizer.vaultapp.worker.FileUploadWorker
import com.vaultionizer.vaultapp.worker.ReferenceFileSyncWorker

fun buildEncryptionWorker(file: VNFile, syncRequestId: Long) =
    prepareFileWorkerBuilder<DataEncryptionWorker>(file, buildSyncWorkData(syncRequestId))
        .addTag(Constants.WORKER_TAG_ENCRYPTION)
        .build()

fun buildUploadWorker(file: VNFile, syncRequestId: Long) =
    prepareFileWorkerBuilder<FileUploadWorker>(file, buildSyncWorkData(syncRequestId))
        .addTag(Constants.WORKER_TAG_UPLOAD)
        .build()

fun buildReferenceFileWorker(file: VNFile) =
    prepareFileWorkerBuilder<ReferenceFileSyncWorker>(
        file,
        workDataOf(
            Constants.WORKER_SPACE_ID to file.space.id
        )
    ).addTag(Constants.WORKER_TAG_REFERENCE_FILE).build()

fun enqueueUniqueFileWork(context: Context, file: VNFile, vararg workers: OneTimeWorkRequest) {
    var chain = WorkManager.getInstance(context).beginUniqueWork(
        String.format(Constants.WORKER_FILE_UNIQUE_NAME_TEMPLATE, file.localId),
        ExistingWorkPolicy.KEEP,
        workers[0]
    )

    for (i in 1 until workers.size) {
        chain = chain.then(workers[i])
    }

    chain.enqueue()
}

inline fun <reified W : ListenableWorker> prepareFileWorkerBuilder(
    file: VNFile,
    inputData: Data
): OneTimeWorkRequest.Builder = OneTimeWorkRequestBuilder<W>()
    .setInputData(inputData)
    .setConstraints(buildDefaultNetworkConstraints())
    .addTag(Constants.WORKER_TAG_FILE)
    .addTag(String.format(Constants.WORKER_TAG_FILE_ID_TEMPLATE, file.localId))

private fun buildSyncWorkData(syncRequestId: Long) = workDataOf(
    Constants.WORKER_SYNC_REQUEST_ID to syncRequestId
)

fun buildDefaultNetworkConstraints() =
    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()