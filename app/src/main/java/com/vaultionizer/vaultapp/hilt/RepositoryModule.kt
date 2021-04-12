package com.vaultionizer.vaultapp.hilt

import android.content.Context
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.repository.*
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.ReferenceFileService
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(userService: UserService, localUserDao: LocalUserDao, gson: Gson) =
        AuthRepository(userService, gson, localUserDao)

    @Provides
    @Singleton
    fun provideReferenceFileRepository(
        referenceFileService: ReferenceFileService,
        gson: Gson,
        localSpaceDao: LocalSpaceDao
    ) = ReferenceFileRepository(referenceFileService, gson, localSpaceDao)

    @Provides
    @Singleton
    fun provideSpaceRepository(
        spaceService: SpaceService,
        localSpaceDao: LocalSpaceDao,
        localFileDao: LocalFileDao,
        gson: Gson
    ) = SpaceRepository(spaceService, localSpaceDao, localFileDao, gson)

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext applicationContext: Context,
        gson: Gson,
        referenceFileRepository: ReferenceFileRepository,
        spaceRepository: SpaceRepository,
        localFileDao: LocalFileDao,
        localSpaceDao: LocalSpaceDao,
        fileExchangeService: FileExchangeService
    ) = FileRepository(
        applicationContext,
        gson,
        referenceFileRepository,
        spaceRepository,
        localFileDao,
        localSpaceDao,
        fileExchangeService
    )

    @Provides
    @Singleton
    fun provideMiscRepository(retrofit: Retrofit) = MiscRepository(retrofit)

    @Provides
    @Singleton
    fun providePCRepository(gson: Gson, fileRepository: FileRepository)
            = PCRepository(gson, fileRepository)
}