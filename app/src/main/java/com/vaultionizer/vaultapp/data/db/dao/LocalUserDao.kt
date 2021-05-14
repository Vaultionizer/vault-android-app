package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.*
import com.vaultionizer.vaultapp.data.db.entity.LocalUser

@Dao
interface LocalUserDao {

    @Query("SELECT * FROM LocalUser")
    fun getAll(): List<LocalUser>

    @Query("SELECT * FROM LocalUser WHERE remote_user_id = :remoteId AND endpoint = :endpoint")
    fun getUserByRemoteId(remoteId: Long, endpoint: String): LocalUser?

    @Query("SELECT * FROM LocalUser WHERE user_id = :id")
    fun getUserById(id: Long): LocalUser?

    @Query("SELECT * FROM LocalUser WHERE last_login = (SELECT MAX(last_login) FROM LocalUser)")
    fun getLastLoggedInUser(): LocalUser?

    @Insert
    fun createUser(localUser: LocalUser): Long

    @Update
    fun updateUsers(vararg users: LocalUser)

    @Query("DELETE FROM LocalUser WHERE user_id = :userId")
    fun deleteUser(userId: Long)
}
