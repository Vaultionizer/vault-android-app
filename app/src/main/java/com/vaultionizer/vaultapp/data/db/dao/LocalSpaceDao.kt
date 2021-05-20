package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.*
import com.vaultionizer.vaultapp.data.db.dao.relation.SpaceWithFiles
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace

@Dao
interface LocalSpaceDao {

    @Query("SELECT * FROM LocalSpace")
    fun getAll(): List<LocalSpace>

    @Query("SELECT * FROM LocalSpace WHERE space_id = :id")
    fun getSpaceById(id: Long): LocalSpace?

    @Query("SELECT * FROM LocalSpace WHERE remote_space_id = :remoteSpaceId AND user_id = :userId")
    suspend fun getSpaceByRemoteId(userId: Long, remoteSpaceId: Long): LocalSpace?

    @Query("SELECT MAX(rowid)+1 FROM LocalSpace")
    suspend fun getNextSpaceId(): Long

    @Transaction
    @Query("SELECT * FROM LocalSpace")
    fun getSpacesWithFiles(): List<SpaceWithFiles>

    @Transaction
    @Query("SELECT * FROM LocalSpace WHERE space_id = :id")
    fun getSpaceWithFiles(id: Long): SpaceWithFiles

    @Insert
    fun createSpace(localSpace: LocalSpace): Long

    @Update
    fun updateSpaces(vararg space: LocalSpace)

    @Delete
    fun deleteSpaces(vararg spaces: LocalSpace)

    @Query("DELETE FROM LocalSpace WHERE user_id = :userId")
    fun quitAllSpaces(userId: Long)

    @Query("SELECT remote_space_id FROM LocalSpace WHERE user_id = :userId")
    fun getAllSpacesWithUser(userId: Long): List<Long>
}