package com.vaultionizer.vaultapp.ui.main.status

import androidx.work.WorkInfo
import com.vaultionizer.vaultapp.data.model.domain.VNFile

data class FileWorkerStatusPair(
    val file: VNFile,
    val status: WorkInfo.State,
    val error: String? = null
)
