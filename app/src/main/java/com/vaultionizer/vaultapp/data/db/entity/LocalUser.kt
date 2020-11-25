package com.vaultionizer.vaultapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalUser(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "remote_user_id")
    val remoteUserId: Long,

    @ColumnInfo(name = "user_name")
    val username: String,

    @ColumnInfo(name = "endpoint")
    val endpoint: String,

    @ColumnInfo(name = "last_login")
    var lastLogin: Long
)