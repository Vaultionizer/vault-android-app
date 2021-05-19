package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    var cryptographicOperationDone: Boolean = false,

    @ColumnInfo(name = "remote_file_id")
    var remoteFileId: Long?,

    /**
     * Only present if [type] is [LocalFileSyncRequest.Type.UPLOAD].
     */
    @ColumnInfo(name = "url")
    var uri: String?
) {

    enum class Type(val id: Int) {
        DOWNLOAD(0),
        UPLOAD(1)
    }
}