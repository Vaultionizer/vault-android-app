package com.vaultionizer.vaultapp.data.model.domain

import android.content.Context
import android.webkit.MimeTypeMap
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.util.Constants
import java.io.File

class VNFile(
    val name: String,
    val space: VNSpace,
    var localId: Long,
    val parent: VNFile? = null
) {

    enum class State {
        AVAILABLE_REMOTE,
        AVAILABLE_OFFLINE,
        UPLOADING,
        DOWNLOADING,
        ENCRYPTING,
        DECRYPTING
    }

    var remoteId: Long? = null

    var content: MutableList<VNFile>? = null
    val isFolder: Boolean
        get() = content != null

    /**
     * Metadata and status
     */
    var lastUpdated: Long = System.currentTimeMillis()
    var createdAt: Long = System.currentTimeMillis()
    var lastSyncTimestamp: Long = System.currentTimeMillis()
    var state: State? = State.AVAILABLE_REMOTE
    val isBusy: Boolean
        get() = state == State.DOWNLOADING || state == State.UPLOADING || state == State.ENCRYPTING || state == State.DECRYPTING

    /**
     * Extension information
     */
    val extension: String?
        get() = MimeTypeMap.getFileExtensionFromUrl(name)
    val mimeType: String?
        get() = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    val isImage: Boolean
        get() = extension?.toLowerCase() in setOf("png", "jpg", "jpeg")

    // TODO(jatsqi) Refactor constructors
    constructor(
        name: String,
        space: VNSpace,
        parent: VNFile?,
        localId: Long,
        remoteId: Long? = null,
        content: MutableList<VNFile>? = null
    ) : this(name, space, localId, parent) {
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
        if (!isFolder && remoteId == null) {
            throw RuntimeException("Invalid operation")
        }

        if (isFolder) {
            return NetworkFolder(
                name = name,
                id = remoteId ?: -1,
                createdAt = createdAt,
                content = content?.filter {
                    !(!it.isFolder && it.remoteId == null)
                }?.map { it.mapToNetwork() }?.toMutableList()
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

    fun mapToLocal(): LocalFile? {
        return LocalFile(
            localId, // Room treats 0 as not-set
            space.id,
            remoteId,
            parent?.localId ?: -1,
            name,
            if (isFolder) LocalFile.Type.FOLDER else LocalFile.Type.FILE,
            lastUpdated,
            createdAt,
            lastSyncTimestamp
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VNFile

        if (localId != other.localId) return false

        return true
    }

    override fun hashCode(): Int {
        return localId.hashCode()
    }


}