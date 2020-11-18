package com.vaultionizer.vaultapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalUser

@Database(entities = arrayOf(LocalUser::class), version = 1)
abstract class VaultionizerLocalDatabase : RoomDatabase() {

    abstract fun localUserDao(): LocalUserDao

}