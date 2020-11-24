package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace

@Dao
interface LocalSpaceDao {

    @Query("SELECT * FROM LocalSpace")
    fun getAll(): List<LocalSpace>

    @Query("SELECT * FROM LocalSpace WHERE space_id = :id")
    fun getSpaceById(id: Long): LocalSpace?

    @Insert
    fun createSpace(localSpace: LocalSpace)

    @Update
    fun updateSpaces(vararg space: LocalSpace)

}