package com.vaultionizer.vaultapp.data.model.domain

import android.content.Context
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.util.Constants
import java.io.File

class VNFile(
    val name: String,
    val space: VNSpace,
    val parent: VNFile? = null
) {

    enum class State {
        AVAILABLE_REMOTE,
        AVAILABLE_OFFLINE,
        PARENT_AVAILABLE_OFFLINE,

        UPLOADING,
        DOWNLOADING
    }

    var localId: Long? = null
    var remoteId: Long? = null
        private set

    var content: MutableList<VNFile>? = null
    val isFolder: Boolean
        get() = content != null && remoteId == null

    // ==== Meta ====
    var lastUpdated: Long? = null
    var createdAt: Long? = null
    var lastSyncTimestamp: Long? = null
    var state: State? = State.AVAILABLE_REMOTE

    // TODO(jatsqi) Refactor constructors
    constructor(
        name: String,
        space: VNSpace,
        parent: VNFile?,
        localId: Long? = null,
        remoteId: Long? = null,
        content: MutableList<VNFile>? = null
    ) : this(name, space, parent) {
        this.localId = localId
        this.remoteId = remoteId
        this.content = content
    }

    fun isDownloaded(ctx: Context): Boolean {
        if (isFolder) return false
        if (localId == null) return false
        if (state == State.DOWNLOADING || state == State.UPLOADING) return false

        val path = File(ctx.filesDir, "$localId.${Constants.VN_FILE_SUFFIX}")
        return path.exists()
    }

    fun mapToNetwork(): NetworkElement {
        if (isFolder) {
            return NetworkFolder(
                name = name,
                id = localId!!,
                createdAt = createdAt,
                content = content?.map { it.mapToNetwork() }?.toMutableList()
            )
        } else { // TODO(jatsqi) Pass correct values to constructor (crc, ...)
            return NetworkFile(
                name = name,
                id = remoteId!!,
                crc = "",
                createdAt = createdAt!!,
                updatedAt = lastUpdated!!,
                size = 0
            )
        }
    }
}