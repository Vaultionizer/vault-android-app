package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.space.*
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface SpaceService {

    @POST("api/spaces/create")
    suspend fun createSpace(@Body createSpaceReq: CreateSpaceRequest): ApiResult<SpaceAuthPair>

    @POST("api/spaces/getAll")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAll(): ApiResult<List<SpaceEntry>>

    @POST("api/spaces/join")
    suspend fun join(@Body joinSpaceReq: JoinSpaceRequest): ApiResult<Nothing>

    @POST("api/space/key")
    suspend fun getAuthKey(@Body getAuthKeyReq: GetAuthKeyRequest): Call<SpaceAuthPair>

    @DELETE("api/space/delete/{spaceID}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun deleteSpace(@Path("spaceID") spaceID : Long)

}