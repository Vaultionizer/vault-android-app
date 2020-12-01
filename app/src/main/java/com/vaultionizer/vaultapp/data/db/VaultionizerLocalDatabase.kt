package com.vaultionizer.vaultapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.db.entity.LocalUser

@Database(entities = arrayOf(LocalUser::class, LocalSpace::class, LocalFile::class), version = 1)
abstract class VaultionizerLocalDatabase : RoomDatabase() {

    abstract fun localUserDao(): LocalUserDao
    abstract fun localSpaceDao(): LocalSpaceDao
    abstract fun localFileDao(): LocalFileDao

}