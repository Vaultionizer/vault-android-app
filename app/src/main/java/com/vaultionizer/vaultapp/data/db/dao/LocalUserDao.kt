package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vaultionizer.vaultapp.data.db.entity.LocalUser

@Dao
interface LocalUserDao {

    @Query("SELECT * FROM LocalUser")
    fun getAll(): List<LocalUser>

    @Query("SELECT * FROM LocalUser WHERE user_name = :username")
    fun getUserByName(username: String): LocalUser?

    @Query("SELECT * FROM LocalUser WHERE user_id = :id")
    fun getUserById(id: Long): LocalUser?

    @Query("SELECT * FROM LocalUser WHERE last_login = (SELECT MAX(last_login) FROM LocalUser)")
    fun getLastLoggedInUser(): LocalUser?

    @Insert
    fun createUser(localUser: LocalUser)

    @Update
    fun updateUsers(vararg users: LocalUser)
}
