package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vaultionizer.vaultapp.data.db.entity.SharedSpaceSecret

@Dao
interface SharedSpaceSecretDao {
    @Insert
    fun createSharedSecret(shared: SharedSpaceSecret)

    @Query("SELECT * FROM SharedSpaceSecret WHERE space_id = :localSpaceId")
    fun getSecret(localSpaceId: Long): SharedSpaceSecret
}