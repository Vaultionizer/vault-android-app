package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SharedSpaceSecret (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "space_id")
    var spaceId: Long,

    @ColumnInfo(name = "secret", typeAffinity = ColumnInfo.BLOB)
    val secret: ByteArray
)