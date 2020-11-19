package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class LocalSpace(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "space_id")
    val spaceId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "last_access")
    val lastAccess: Long
)