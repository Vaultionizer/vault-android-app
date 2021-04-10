package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest

@Dao
interface LocalFileSyncRequestDao {

    @Query("SELECT * FROM LocalFileSyncRequest")
    fun getAll(): List<LocalFileSyncRequest>

    @Query("SELECT * FROM LocalFileSyncRequest WHERE request_id = :id")
    fun getById(id: Long): LocalFileSyncRequest

}