package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest.Type

@Entity
data class LocalFileSyncRequest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "request_id")
    var requestId: Long,

    @ColumnInfo(name = "type")
    val type: Type,

    @ColumnInfo(name = "local_file_id")
    val localFileId: Long,

    @ColumnInfo(name = "crypto_opt_done")
    val cryptographicOperationDone: Boolean = false,

    @ColumnInfo(name = "remote_file_id")
    val remoteFileId: Long?,

    /**
     * URI to the file in the local file system.
     * Only present if [type] is [Type.UPLOAD] and [isVirtualFolder] is false.
     */
    @ColumnInfo(name = "uri", typeAffinity = ColumnInfo.BLOB)
    val data: ByteArray? = null,
) {

    enum class Type(val id: Int) {
        DOWNLOAD(0),
        UPLOAD(1)
    }
}