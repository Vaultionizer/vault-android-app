package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.request.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.user.NetworkUserAuthPair
import retrofit2.http.*
import java.util.*

interface UserService {
    @POST("api/user/create")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): ApiResult<NetworkUserAuthPair>

    @POST("api/user/login")
    suspend fun loginUser(@Body loginUserRequest: LoginUserRequest): ApiResult<NetworkUserAuthPair>

    @DELETE("api/user")
    suspend fun deleteUser(@Body objects: Objects)

    @PUT("api/user/logout")
    suspend fun logoutUser(@Body objects: Objects)
}