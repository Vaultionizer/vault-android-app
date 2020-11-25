package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.request.JoinSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.space.*
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SpaceService {

    @POST("api/spaces/create")
    suspend fun createSpace(@Body createSpaceRequest: CreateSpaceRequest): ApiResult<NetworkSpaceAuthPair>

    @POST("api/spaces/getAll")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAll(): ApiResult<List<NetworkSpace>>

    @POST("api/spaces/join")
    suspend fun join(@Body joinSpaceRequest: JoinSpaceRequest): ApiResult<Nothing>

    // @POST("space/key")
    // fun getAuthKey(@Body getAuthKeyRequest: GetAuthKeyRequest): Call<SpaceAuthPair>

}