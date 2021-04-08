package com.vaultionizer.vaultapp.hilt

import android.content.Context
import androidx.room.Room
import com.vaultionizer.vaultapp.data.db.VaultionizerLocalDatabase
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): VaultionizerLocalDatabase =
        Room.databaseBuilder(context, VaultionizerLocalDatabase::class.java, "vault-local").build()

    @Provides
    fun provideUserDao(database: VaultionizerLocalDatabase): LocalUserDao = database.localUserDao()

    @Provides
    fun provideSpaceDao(database: VaultionizerLocalDatabase): LocalSpaceDao =
        database.localSpaceDao()

    @Provides
    fun provideFileDao(database: VaultionizerLocalDatabase): LocalFileDao = database.localFileDao()

}