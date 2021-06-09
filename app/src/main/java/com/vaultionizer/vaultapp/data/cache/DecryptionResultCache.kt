package com.vaultionizer.vaultapp.data.cache

import com.hadilq.liveevent.LiveEvent
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import okhttp3.internal.EMPTY_BYTE_ARRAY

class DecryptionResultCache {

    // TODO(jatsqi): Refactor to SoftReference/WeakReference.
    private val fileDataPairs = mutableSetOf<DecryptionFileDataPair>()

    private val decryptionResults_ = LiveEvent<Set<DecryptionFileDataPair>>()
    val decryptionResultsLiveData = decryptionResults_

    fun addDecryptionResult(file: VNFile, data: ByteArray) {
        fileDataPairs.add(DecryptionFileDataPair(file, data))
        decryptionResultsLiveData.postValue(fileDataPairs)
    }

    fun invalidateResult(file: VNFile) {
        if (fileDataPairs.remove(DecryptionFileDataPair(file, EMPTY_BYTE_ARRAY))) {
            decryptionResults_.value = fileDataPairs
        }
    }

    fun invalidateResultByFileId(fileId: Long) {
        val iterator = fileDataPairs.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.file.localId == fileId) {
                iterator.remove()
            }
        }
    }

    fun getResultByFileId(fileId: Long): ByteArray? =
        fileDataPairs.firstOrNull { it.file.localId == fileId }?.data

}