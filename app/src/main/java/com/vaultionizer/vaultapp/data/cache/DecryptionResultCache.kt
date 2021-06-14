package com.vaultionizer.vaultapp.data.cache

import androidx.lifecycle.MutableLiveData
import com.vaultionizer.vaultapp.data.model.domain.VNFile

class DecryptionResultCache {

    // TODO(jatsqi): Refactor to SoftReference/WeakReference.
    private val fileDataPairs = mutableSetOf<DecryptionFileDataPair>()

    private val decryptionResults_ = MutableLiveData<Set<DecryptionFileDataPair>>()
    val decryptionResultsLiveData = decryptionResults_

    fun addDecryptionResult(file: VNFile, data: ByteArray) {
        fileDataPairs.add(DecryptionFileDataPair(file, data, false))
        updateLiveData()
    }

    fun invalidateResultByFileId(fileId: Long) {
        invalidateResultsBulk(setOf(fileId))
    }

    fun invalidateResultsBulk(ids: Set<Long>) {
        val iterator = fileDataPairs.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (ids.contains(element.file.localId)) {
                iterator.remove()
            }
        }

        if (ids.size > 0) {
            updateLiveData()
        }
    }

    fun invalidateShown() {
        val iterator = fileDataPairs.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.shown) {
                iterator.remove()
            }
        }

        updateLiveData()
    }

    fun getResultByFileId(fileId: Long): ByteArray? =
            fileDataPairs.firstOrNull { it.file.localId == fileId }?.data

    fun annotateResultAsShown(fileId: Long) {
        fileDataPairs.firstOrNull { it.file.localId == fileId }?.shown = true
    }

    private fun updateLiveData() {
        decryptionResults_.postValue(fileDataPairs)
    }
}