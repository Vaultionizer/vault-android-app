package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI

@Entity
data class LocalFileSyncRequest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "request_id")
    var requestId: Long,

    @ColumnInfo(name = "type")
    val type: Type,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "space_id")
    val spaceId: Long,

    /**
     * URI to the file in the local file system.
     * Only present if [type] is [Type.UPLOAD] and [isVirtualFolder] is false.
     */
    @ColumnInfo(name = "uri")
    val uri: String?,

    /**
     * Remote ID of the file.
     *
     * If [type] is [Type.DOWNLOAD], then this id must be present.
     *
     * If [type] is [Type.UPLOAD] and this id is present, then the application already requested
     * an file id but got interrupted during the upload process. Otherwise the upload has to be
     * requested.
     */
    @ColumnInfo(name = "remote_file_id")
    var remoteFileId: Long?,

    @ColumnInfo(name = "local_file_id")
    var localFileId: Long?
) {

    enum class Type(val id: Int) {
        DOWNLOAD(0),
        UPLOAD(1)
    }
}