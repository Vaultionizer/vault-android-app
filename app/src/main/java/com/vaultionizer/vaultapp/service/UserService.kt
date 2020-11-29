package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.user.*

import retrofit2.http.*
import java.util.*


interface UserService {
    @POST("api/users/create")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): ApiResult<UserAuthPair>

    @POST("api/users/login")
    suspend fun loginUser(@Body loginUserRequest: LoginUserRequest): ApiResult<UserAuthPair>

    @DELETE("api/users/delete")
    suspend fun deleteUser(@Body objects: Objects)

    @PUT("api/users/logout")
    suspend fun logoutUser(@Body objects: Objects)
}