package com.vaultionizer.vaultapp.hilt

import com.vaultionizer.vaultapp.data.source.MiscService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import retrofit2.Retrofit

@Module
@InstallIn(ActivityComponent::class)
object MiscModule {

    @Provides
    fun provideMiscService(retrofit: Retrofit): MiscService = retrofit.create(MiscService::class.java)

}