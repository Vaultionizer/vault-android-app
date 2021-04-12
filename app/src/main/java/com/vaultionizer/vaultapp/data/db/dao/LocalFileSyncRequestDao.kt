package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest

@Dao
interface LocalFileSyncRequestDao {

    @Query("SELECT * FROM LocalFileSyncRequest")
    fun getAll(): List<LocalFileSyncRequest>

    @Query("SELECT * FROM LocalFileSyncRequest WHERE request_id = :id")
    fun getById(id: Long): LocalFileSyncRequest

    @Insert
    fun createRequest(request: LocalFileSyncRequest): Long

    @Query("UPDATE LocalFileSyncRequest SET remote_file_id = :remoteFileId WHERE request_id = :requestId")
    fun updateRemoteFileId(requestId: Long, remoteFileId: Long)

    @Update
    fun updateRequest(request: LocalFileSyncRequest)

}