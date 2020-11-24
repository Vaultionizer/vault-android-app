package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalSpace(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "space_id")
    val spaceId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "reference_file")
    val referenceFile: String,

    @ColumnInfo(name = "last_access")
    val lastAccess: Long
)