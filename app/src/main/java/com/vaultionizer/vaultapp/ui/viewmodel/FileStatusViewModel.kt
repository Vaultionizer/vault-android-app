package com.vaultionizer.vaultapp.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.ui.main.status.FileWorkerStatusPair
import com.vaultionizer.vaultapp.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileStatusViewModel @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    val fileRepository: FileRepository
) : ViewModel() {

    companion object {
        val WORKER_QUERY = WorkQuery.Builder
            .fromTags(
                listOf(
                    Constants.WORKER_TAG_FILE
                )
            ).build()

        val WORKER_STATUS_WEIGHT_MAP = mapOf(
            WorkInfo.State.RUNNING to 4,
            WorkInfo.State.ENQUEUED to 3,
            WorkInfo.State.FAILED to 2,
            WorkInfo.State.CANCELLED to 2,
            WorkInfo.State.BLOCKED to 2,
            WorkInfo.State.SUCCEEDED to 1,
        )
    }

    val workInfo =
        WorkManager.getInstance(applicationContext).getWorkInfosLiveData(WORKER_QUERY)
    private val fileStatus_ = MutableLiveData<List<FileWorkerStatusPair>>(emptyList())
    val fileStatus: LiveData<List<FileWorkerStatusPair>> = fileStatus_

    fun onWorkerStatusChange(workInfoList: List<WorkInfo>) {
        viewModelScope.launch {
            // WorkManager.getInstance(applicationContext).pruneWork()
            val newStatus = mutableListOf<FileWorkerStatusPair>()
            val fileMap = mutableMapOf<VNFile, MutableList<WorkInfo>>()

            outer@ for (info in workInfoList) {
                for (tag in info.tags) {
                    if (!tag.startsWith(Constants.WORKER_TAG_FILE_ID_TEMPLATE_BEGIN)) {
                        continue
                    }

                    val file = fileRepository.getFile(
                        tag.replace(
                            Constants.WORKER_TAG_FILE_ID_TEMPLATE_BEGIN,
                            ""
                        ).toLong()
                    )

                    if (file != null) {
                        val list = fileMap[file] ?: mutableListOf()
                        list.add(info)
                        fileMap[file] = list
                    }
                }
            }

            for (fileStatusPair in fileMap) {
                var allWorkersFinished = fileStatusPair.value[0].state == WorkInfo.State.SUCCEEDED
                val mostValuableState = fileStatusPair.value.maxByOrNull {
                    if (it.state != WorkInfo.State.SUCCEEDED) {
                        allWorkersFinished = false
                    }
                    return@maxByOrNull WORKER_STATUS_WEIGHT_MAP[it.state]!!
                }

                if (allWorkersFinished) {
                    resetFileStatus(fileStatusPair.key)
                } else {
                    mostValuableState?.let {
                        adjustFileStatus(fileStatusPair.key, it)
                        newStatus.add(FileWorkerStatusPair(fileStatusPair.key, it.state))
                    }
                }
            }

            fileStatus_.postValue(newStatus)
        }
    }

    private fun resetFileStatus(file: VNFile) {
        Log.e("Vault", "Changing state!")
        file.state = if (file.isDownloaded(applicationContext))
            VNFile.State.AVAILABLE_OFFLINE
        else
            VNFile.State.AVAILABLE_REMOTE
    }

    private fun adjustFileStatus(file: VNFile, workInfo: WorkInfo) {
        if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
            if (workInfo.tags.contains(Constants.WORKER_TAG_DECRYPTION))
                file.state = VNFile.State.DECRYPTING
            else if (workInfo.tags.contains(Constants.WORKER_TAG_ENCRYPTION))
                file.state = VNFile.State.ENCRYPTING
            else if (workInfo.tags.contains(Constants.WORKER_TAG_DOWNLOAD)) {
                file.state = VNFile.State.DOWNLOADING
            }
        }
    }
}