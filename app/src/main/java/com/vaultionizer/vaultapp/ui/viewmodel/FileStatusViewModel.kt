package com.vaultionizer.vaultapp.ui.viewmodel

import android.content.Context
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
            )
            .addStates(
                listOf(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.FAILED
                )
            )
            .build()
    }

    val workInfo =
        WorkManager.getInstance(applicationContext).getWorkInfosLiveData(WORKER_QUERY)
    private val fileStatus_ = MutableLiveData<List<FileWorkerStatusPair>>(emptyList())
    val fileStatus: LiveData<List<FileWorkerStatusPair>> = fileStatus_

    fun onWorkerStatusChange(workInfoList: List<WorkInfo>) {
        viewModelScope.launch {
            val newStatus = mutableListOf<FileWorkerStatusPair>()

            for (info in workInfoList) {
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
                        newStatus.add(FileWorkerStatusPair(file, info.state))
                        adjustFileStatus(file, info)
                    }

                    break
                }
            }

            fileStatus_.postValue(newStatus)
        }

        WorkManager.getInstance(applicationContext).pruneWork()
    }

    private fun adjustFileStatus(file: VNFile, workInfo: WorkInfo) {
        if (workInfo.state == WorkInfo.State.FAILED || workInfo.state == WorkInfo.State.CANCELLED || workInfo.state == WorkInfo.State.SUCCEEDED) {
            file.state = if (file.isDownloaded(applicationContext))
                VNFile.State.AVAILABLE_OFFLINE
            else
                VNFile.State.AVAILABLE_REMOTE
            return
        }

        if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
            file.state = if (workInfo.tags.contains(Constants.WORKER_TAG_DECRYPTION))
                VNFile.State.DECRYPTING
            else if (workInfo.tags.contains(Constants.WORKER_TAG_ENCRYPTION))
                VNFile.State.ENCRYPTING
            else
                return
        }
    }
}