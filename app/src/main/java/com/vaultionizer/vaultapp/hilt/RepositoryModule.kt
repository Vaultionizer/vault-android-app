package com.vaultionizer.vaultapp.hilt

import android.content.Context
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalFileSyncRequestDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.repository.*
import com.vaultionizer.vaultapp.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
        localSpaceDao: LocalSpaceDao,
        spaceRepository: SpaceRepository
    ) = ReferenceFileRepository(
        referenceFileService,
        gson,
        localSpaceDao,
        spaceRepository
    )

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
        fileService: FileService,
        syncRequestService: SyncRequestService
    ) = FileRepository(
        applicationContext,
        gson,
        referenceFileRepository,
        spaceRepository,
        localFileDao,
        localSpaceDao,
        fileService,
        syncRequestService
    )

    @Provides
    @Singleton
    fun provideMiscRepository(retrofit: Retrofit) = MiscRepository(retrofit)

    @Provides
    @Singleton
    fun provideSyncRequestService(localFileSyncRequestDao: LocalFileSyncRequestDao) =
        SyncRequestService(localFileSyncRequestDao)
  
    @Provides
    @Singleton
    fun providePCRepository(gson: Gson, fileRepository: FileRepository)
            = PCRepository(gson, fileRepository)


}