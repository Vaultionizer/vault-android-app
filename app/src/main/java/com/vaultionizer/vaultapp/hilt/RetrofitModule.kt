package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideMiscService(retrofit: Retrofit): MiscService =
        retrofit.create(MiscService::class.java)

    @Provides
    fun provideSpaceService(retrofit: Retrofit): SpaceService =
        retrofit.create(SpaceService::class.java)

    @Provides
    fun provideUserService(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

    @Provides
    fun provideReferenceFileService(retrofit: Retrofit) =
        retrofit.create(ReferenceFileService::class.java)

    @Provides
    fun provideFileService(retrofit: Retrofit) = retrofit.create(FileService::class.java)

    @Provides
    @Singleton
    fun provideFileExchangeService(
        authRepository: AuthRepository,
        fileService: FileService,
        gson: Gson
    ) = FileExchangeService(authRepository, fileService, gson)

}