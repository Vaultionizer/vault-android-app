package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.request.GetAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.request.JoinSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.space.*
import retrofit2.Response
import retrofit2.http.*

interface SpaceService {

    @POST("api/spaces/create")
    suspend fun createSpace(@Body createSpaceReq: CreateSpaceRequest): ApiResult<Long>

    @POST("api/spaces/getAll")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAll(): ApiResult<List<NetworkSpace>>

    @POST("api/spaces/join")
    suspend fun join(@Body joinSpaceReq: JoinSpaceRequest): ApiResult<Nothing>

    @POST("api/space/key")
    suspend fun getAuthKey(@Body getAuthKeyReq: GetAuthKeyRequest): ApiResult<NetworkSpaceAuthPair>

    @DELETE("api/spaces/delete/{spaceID}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun deleteSpace(@Path("spaceID") spaceID : Long): ApiResult<String>

}