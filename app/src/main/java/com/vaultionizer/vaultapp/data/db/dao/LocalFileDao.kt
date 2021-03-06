package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.vaultionizer.vaultapp.data.db.entity.LocalFile

@Dao
interface LocalFileDao {

    @Query("SELECT * FROM LocalFile")
    suspend fun getAll(): List<LocalFile>

    @Query("SELECT * FROM LocalFile WHERE file_id = :id")
    suspend fun getFileById(id: Long): LocalFile?

    @Query("SELECT * FROM LocalFile WHERE space_id = :spaceId AND remote_file_id = :remoteId")
    suspend fun getFileByRemoteId(spaceId: Long, remoteId: Long): LocalFile?

    @Insert
    suspend fun createFile(localFile: LocalFile): Long

    @Delete
    suspend fun deleteFiles(vararg files: LocalFile)

    @Query("DELETE FROM LocalFile WHERE file_id = :localId")
    suspend fun deleteFile(localId: Long)

    @Query("DELETE FROM LocalFile WHERE space_id = :spaceId")
    suspend fun deleteFilesBySpace(spaceId: Long)

    @Query("DELETE FROM LocalFile WHERE space_id = :spaceId AND file_id NOT IN (:ids) AND remote_file_id IS NOT NULL")
    suspend fun deleteAllFilesOfSpaceExceptWithIds(ids: Set<Long>, spaceId: Long)

    @Query("DELETE FROM LocalFile WHERE space_id IN (SELECT space.space_id FROM LocalSpace space WHERE space.user_id = :userId)")
    suspend fun deleteAllFilesOfUser(userId: Long)

    @Query("UPDATE LocalFile SET remote_file_id = :remoteId WHERE file_id = :fileId")
    suspend fun updateFileRemoteId(fileId: Long, remoteId: Long)

}