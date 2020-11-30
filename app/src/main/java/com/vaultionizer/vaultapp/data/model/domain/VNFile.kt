package com.vaultionizer.vaultapp.data.model.domain

import android.content.Context
import com.vaultionizer.vaultapp.util.Constants
import java.io.File

class VNFile(
    val name: String,
    var remoteId: Long? = null,
    var localId: Long? = null) {

    var isFolder: Boolean = false
        get() = remoteId == null
        private set
    var parentId: Long? = null

    // ==== Meta ====
    var lastUpdated: Long? = null
    var createdAt: Long? = null
    var isDownloading: Boolean = false
    var lastSyncTimestamp: Long? = null

    fun isDownloaded(ctx: Context): Boolean {
        if(isFolder) return false
        if(localId == null) return false

        val path = File(ctx.filesDir, "$localId.${Constants.VN_FILE_SUFFIX}")
        return path.exists()
    }
}