package com.vaultionizer.vaultapp.hilt

import com.vaultionizer.vaultapp.data.source.MiscService
import com.vaultionizer.vaultapp.data.source.SpaceService
import com.vaultionizer.vaultapp.data.source.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import retrofit2.Retrofit
import retrofit2.http.GET

@Module
@InstallIn(ActivityComponent::class)
object RestServiceModule {

    @Provides
    fun provideMiscService(retrofit: Retrofit): MiscService = retrofit.create(MiscService::class.java)

    @Provides
    fun provideSpaceService(retrofit: Retrofit): SpaceService = retrofit.create(SpaceService::class.java)

    @Provides
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

}