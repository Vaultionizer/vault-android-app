package com.vaultionizer.vaultapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.db.entity.LocalUser
import com.vaultionizer.vaultapp.data.db.entity.converters.EnumConverters

@Database(
    entities = arrayOf(
        LocalUser::class,
        LocalSpace::class,
        LocalFile::class,
        LocalFileSyncRequest::class
    ), version = 1
)
@TypeConverters(EnumConverters::class)
abstract class VaultionizerLocalDatabase : RoomDatabase() {

    abstract fun localUserDao(): LocalUserDao
    abstract fun localSpaceDao(): LocalSpaceDao
    abstract fun localFileDao(): LocalFileDao
    abstract fun localFileSyncRequestDao(): LocalFileSyncRequestDao
}