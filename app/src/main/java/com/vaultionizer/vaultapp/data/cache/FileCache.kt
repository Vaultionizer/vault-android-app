package com.vaultionizer.vaultapp.data.cache

import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.repository.FileRepository

class FileCache(private val strategy: IdCachingStrategy = IdCachingStrategy.LOCAL_ID) {

    enum class IdCachingStrategy {
        LOCAL_ID,
        REMOTE_ID
    }

    var spaceId: Long? = null
        private set

    val rootFile: VNFile?
        get() = getFileByStrategy(FileRepository.ROOT_FOLDER_ID, IdCachingStrategy.LOCAL_ID)

    private var files = mutableMapOf<Long, VNFile>()

    fun addFile(file: VNFile): Boolean {
        if (!checkConstraints(file)) {
            return false
        }

        when (strategy) {
            IdCachingStrategy.LOCAL_ID -> files[file.localId] = file
            IdCachingStrategy.REMOTE_ID -> files[file.remoteId!!] = file
        }

        return true
    }

    fun deleteFile(file: VNFile): Boolean {
        if (!checkConstraints(file)) {
            return false
        }

        when (strategy) {
            IdCachingStrategy.LOCAL_ID -> files.remove(file.localId)
            IdCachingStrategy.REMOTE_ID -> files.remove(file.remoteId)
        }

        return true
    }

    fun getFile(id: Long): VNFile? = files[id]

    fun getFileByStrategy(id: Long, strategy: IdCachingStrategy): VNFile? {
        if (strategy == this.strategy) {
            return getFile(id)
        }

        if (strategy == IdCachingStrategy.LOCAL_ID) {
            return files.values.firstOrNull { id == it.localId }
        }

        return files.values.firstOrNull { id == it.remoteId }
    }

    private fun checkConstraints(file: VNFile): Boolean {
        if (strategy == IdCachingStrategy.REMOTE_ID) {
            if (spaceId == null) {
                spaceId = file.space.id
            } else if (file.space.id != spaceId) {
                return false
            }
        }

        return when (strategy) {
            IdCachingStrategy.LOCAL_ID -> file.localId != null
            IdCachingStrategy.REMOTE_ID -> file.remoteId != null
        }
    }

}