package com.vaultionizer.vaultapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vaultionizer.vaultapp.data.db.dao.*
import com.vaultionizer.vaultapp.data.db.entity.*
import com.vaultionizer.vaultapp.data.db.entity.converters.EnumConverters

@Database(
    entities = [LocalUser::class, LocalSpace::class, LocalFile::class, LocalFileSyncRequest::class, SharedSpaceSecret::class],
    version = 1
)
@TypeConverters(EnumConverters::class)
abstract class VaultionizerLocalDatabase : RoomDatabase() {

    abstract fun localUserDao(): LocalUserDao
    abstract fun localSpaceDao(): LocalSpaceDao
    abstract fun localFileDao(): LocalFileDao
    abstract fun localFileSyncRequestDao(): LocalFileSyncRequestDao
    abstract fun sharedSpaceSecretDao(): SharedSpaceSecretDao
}