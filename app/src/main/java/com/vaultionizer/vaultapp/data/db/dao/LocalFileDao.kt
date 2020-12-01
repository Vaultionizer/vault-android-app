package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vaultionizer.vaultapp.data.db.entity.LocalFile

@Dao
interface LocalFileDao {

    @Query("SELECT * FROM LocalFile")
    fun getAll(): List<LocalFile>

    @Query("SELECT * FROM LocalFile WHERE file_id = :id")
    fun getFileById(id: Long): LocalFile?

    @Insert
    fun createFile(localFile: LocalFile): Long

}