package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalFile(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "file_id")
    val fileId: Long,

    @ColumnInfo(name = "space_id")
    val spaceId: Long,

    /**
     * File ID on the remote server.
     * If this id is NULL, then this file has not been uploaded yet.
     */
    @ColumnInfo(name = "remote_file_id")
    val remoteFileId: Long?,

    @ColumnInfo(name = "parent_file_id")
    var parentFileId: Long,

    @ColumnInfo(name = "file_name")
    val name: String,

    @ColumnInfo(name = "file_type")
    val type: Type,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_sync")
    val lastSyncTimestamp: Long
) {
    enum class Type(val id: Int) {
        FILE(0),
        FOLDER(1)
    }
}