package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    // @Provides
    // fun provideAuthRepository(userService: UserService) = AuthRepository(userService, RestModule.provideGson())

}