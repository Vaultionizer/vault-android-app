package com.vaultionizer.vaultapp.hilt

import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideAuthCache() = AuthCache()

    @Provides
    @Singleton
    fun provideDecryptionCache() = DecryptionResultCache()

}