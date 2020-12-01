package com.vaultionizer.vaultapp.hilt

import com.vaultionizer.vaultapp.service.MiscService
import com.vaultionizer.vaultapp.service.ReferenceFileService
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import retrofit2.Retrofit

@Module
@InstallIn(ApplicationComponent::class)
object RetrofitModule {

    @Provides
    fun provideMiscService(retrofit: Retrofit): MiscService = retrofit.create(MiscService::class.java)

    @Provides
    fun provideSpaceService(retrofit: Retrofit): SpaceService = retrofit.create(SpaceService::class.java)

    @Provides
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

    @Provides
    fun provideReferenceFileService(retrofit: Retrofit) = retrofit.create(ReferenceFileService::class.java)

}