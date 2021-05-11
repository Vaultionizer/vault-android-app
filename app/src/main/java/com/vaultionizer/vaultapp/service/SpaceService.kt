package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.*
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpaceAuthPair
import retrofit2.http.*

interface SpaceService {

    @POST("api/spaces/create")
    suspend fun createSpace(@Body createSpaceReq: CreateSpaceRequest): ApiResult<Long>

    @POST("api/spaces/getAll")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAll(): ApiResult<List<NetworkSpace>>

    @POST("api/spaces/join")
    suspend fun join(@Body joinSpaceReq: JoinSpaceRequest): ApiResult<Nothing>

    @POST("api/spaces/key")
    suspend fun getAuthKey(@Body getAuthKeyReq: GetAuthKeyRequest): ApiResult<NetworkSpaceAuthPair>

    @DELETE("api/spaces/delete/{spaceID}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun deleteSpace(@Path("spaceID") spaceID: Long): ApiResult<String>

    @POST("api/spaces/{spaceID}/config")
    suspend fun configureSpace(@Body config: ConfigureSpaceRequest, @Path("spaceID") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    @POST("api/spaces/{spaceID}/kickall")
    suspend fun kickAllUsers(@Path("spaceID") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    @POST("api/spaces/{spaceID}/config/get")
    suspend fun getSpaceConfig(@Path("spaceID") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    @POST("api/spaces/{spaceID}/authkey")
    suspend fun changeAuthKey(@Body authKey: ChangeAuthKeyRequest, @Path("spaceID") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    @POST("api/spaces/quit/{spaceID}")
    suspend fun quitSpace(@Path("spaceID") spaceID: Long): ApiResult<NetworkSpaceAuthPair>
}