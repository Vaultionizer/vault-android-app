package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.CreateUserRequest
import com.vaultionizer.vaultapp.data.model.rest.request.LoginUserRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.user.NetworkUserAuthPair
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {
    @POST("api/user/create")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): ApiResult<NetworkUserAuthPair>

    @POST("api/user/login")
    suspend fun loginUser(@Body loginUserRequest: LoginUserRequest): ApiResult<NetworkUserAuthPair>

    @DELETE("api/user")
    suspend fun deleteUser()

    @PUT("api/user/logout")
    suspend fun logoutUser()
}