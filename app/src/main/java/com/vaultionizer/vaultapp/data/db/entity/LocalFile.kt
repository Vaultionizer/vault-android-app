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
    val remoteFileId: Long?
)