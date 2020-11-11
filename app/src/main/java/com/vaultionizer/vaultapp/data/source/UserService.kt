package com.vaultionizer.vaultapp.data.source

import com.vaultionizer.vaultapp.data.model.user.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface UserService {

    @POST("users/create")
    fun createUser(@Body createUserRequest: CreateUserRequest): Call<UserAuthPair>

    @POST("users/login")
    fun loginUser(@Body loginUserRequest: LoginUserRequest): Call<UserAuthPair>

}