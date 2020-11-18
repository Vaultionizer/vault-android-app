package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.user.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.user.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.user.UserAuthPair
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface UserService {

    @POST("users/create")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): ApiResult<UserAuthPair>

    @POST("users/login")
    suspend fun loginUser(@Body loginUserRequest: LoginUserRequest): ApiResult<UserAuthPair>

}