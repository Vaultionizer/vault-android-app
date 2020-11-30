package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalSpace(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "space_id")
    var spaceId: Long,

    @ColumnInfo(name = "remote_space_id")
    val remoteSpaceId: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "name")
    var name: String?,

    @ColumnInfo(name = "reference_file")
    var referenceFile: String?,

    @ColumnInfo(name = "last_access")
    var lastAccess: Long
)