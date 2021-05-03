package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.*
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest

@Dao
interface LocalFileSyncRequestDao {

    @Query("SELECT * FROM LocalFileSyncRequest")
    suspend fun getAll(): List<LocalFileSyncRequest>

    @Query("SELECT * FROM LocalFileSyncRequest WHERE request_id = :id")
    suspend fun getById(id: Long): LocalFileSyncRequest

    @Insert
    suspend fun createRequest(request: LocalFileSyncRequest): Long

    @Query("UPDATE LocalFileSyncRequest SET remote_file_id = :remoteFileId WHERE request_id = :requestId")
    suspend fun updateRemoteFileId(requestId: Long, remoteFileId: Long)

    @Update
    suspend fun updateRequest(request: LocalFileSyncRequest)

    @Delete
    suspend fun deleteRequest(request: LocalFileSyncRequest)

}