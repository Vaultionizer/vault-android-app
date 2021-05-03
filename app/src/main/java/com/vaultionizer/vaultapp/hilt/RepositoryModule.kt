package com.vaultionizer.vaultapp.hilt

import com.vaultionizer.vaultapp.repository.*
import com.vaultionizer.vaultapp.repository.impl.*
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.impl.FileExchangeServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideReferenceFileRepository(referenceFileRepositoryImpl: ReferenceFileRepositoryImpl): ReferenceFileRepository

    @Binds
    @Singleton
    abstract fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun provideSpaceRepository(spaceRepositoryImpl: SpaceRepositoryImpl): SpaceRepository

    @Binds
    @Singleton
    abstract fun provideFileRepository(fileRepositoryImpl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun provideMiscRepository(miscRepositoryImpl: MiscRepositoryImpl): MiscRepository

    @Binds
    @Singleton
    abstract fun provideSyncRequestService(syncRequestRepositoryImpl: SyncRequestRepositoryImpl): SyncRequestRepository

    @Binds
    @Singleton
    abstract fun providePCRepository(pcRepositoryImpl: PCRepositoryImpl): PCRepository

    @Binds
    @Singleton
    abstract fun provideFileExchangeService(fileExchangeServiceImpl: FileExchangeServiceImpl): FileExchangeService

}