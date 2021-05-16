package com.vaultionizer.vaultapp.data.cache

import androidx.lifecycle.MutableLiveData
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import okhttp3.internal.EMPTY_BYTE_ARRAY

class DecryptionResultCache {

    private val fileDataPairs = mutableSetOf<DecryptionFileDataPair>()

    private val decryptionResults_ = MutableLiveData<Set<DecryptionFileDataPair>>()
    val decryptionResultsLiveData = decryptionResults_

    fun addDecryptionResult(file: VNFile, data: ByteArray) {
        fileDataPairs.add(DecryptionFileDataPair(file, data))
        decryptionResultsLiveData.postValue(fileDataPairs)
    }

    fun invalidateResult(file: VNFile) {
        if (fileDataPairs.remove(DecryptionFileDataPair(file, EMPTY_BYTE_ARRAY))) {
            decryptionResults_.postValue(fileDataPairs)
        }
    }

    fun getResultByFileId(fileId: Long): ByteArray? =
        fileDataPairs.firstOrNull { it.file.localId == fileId }?.data

}