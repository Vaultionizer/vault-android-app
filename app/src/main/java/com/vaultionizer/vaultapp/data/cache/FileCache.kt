package com.vaultionizer.vaultapp.data.cache

import com.vaultionizer.vaultapp.data.model.domain.VNFile

class FileCache(private val strategy: IdCachingStrategy = IdCachingStrategy.LOCAL_ID) {

    enum class IdCachingStrategy {
        LOCAL_ID,
        REMOTE_ID
    }

    private var files = mutableMapOf<Long, VNFile>()
    private var spaceId: Long? = null

    fun addFile(file: VNFile): Boolean {
        if (!checkConstraints(file)) {
            return false
        }

        when (strategy) {
            IdCachingStrategy.LOCAL_ID -> files.put(file.localId!!, file)
            IdCachingStrategy.REMOTE_ID -> files.put(file.remoteId!!, file)
        }

        return true
    }

    fun getFile(id: Long): VNFile? = files[id]

    private fun checkConstraints(file: VNFile): Boolean {
        if (spaceId == null) {
            spaceId = file.space.id
        } else if (file.space.id != spaceId) {
            return false
        }

        return when (strategy) {
            IdCachingStrategy.LOCAL_ID -> file.localId != null
            IdCachingStrategy.REMOTE_ID -> file.remoteId != null
        }
    }

}