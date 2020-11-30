package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.repository.*
import com.vaultionizer.vaultapp.service.ReferenceFileService
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(userService: UserService, localUserDao: LocalUserDao, gson: Gson) = AuthRepository(userService, gson, localUserDao)

    @Provides
    @Singleton
    fun provideReferenceFileRepository(referenceFileService: ReferenceFileService, gson: Gson, localSpaceDao: LocalSpaceDao) = ReferenceFileRepository(referenceFileService, gson, localSpaceDao)

    @Provides
    @Singleton
    fun provideSpaceRepository(spaceService: SpaceService, localSpaceDao: LocalSpaceDao) = SpaceRepository(spaceService, localSpaceDao)

    @Provides
    @Singleton
    fun provideFileRepository(referenceFileRepository: ReferenceFileRepository, spaceRepository: SpaceRepository, localFileDao: LocalFileDao, localSpaceDao: LocalSpaceDao) = FileRepository(referenceFileRepository, spaceRepository, localFileDao, localSpaceDao)

    @Provides
    @Singleton
    fun provideMiscRepository(retrofit: Retrofit) = MiscRepository(retrofit)
}