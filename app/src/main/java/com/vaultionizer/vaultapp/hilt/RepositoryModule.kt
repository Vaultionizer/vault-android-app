package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.service.ReferenceFileService
import com.vaultionizer.vaultapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
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
    fun provideReferenceFileRepository(referenceFileService: ReferenceFileService, gson: Gson) = ReferenceFileRepository(referenceFileService, gson)

}