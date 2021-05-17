package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.ChangeAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.request.ConfigureSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.request.JoinSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpaceAuthPair
import retrofit2.http.*

interface SpaceService {

    @DELETE("api/space/{remoteSpaceId}")
    suspend fun deleteSpace(@Path("remoteSpaceId") spaceID: Long): ApiResult<Nothing>

    @GET("api/space/{remoteSpaceId}/authkey")
    suspend fun getAuthKey(@Path("remoteSpaceId") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    @PUT("api/space/{remoteSpaceId}/authkey")
    suspend fun changeAuthKey(
        @Body authKey: ChangeAuthKeyRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<NetworkSpaceAuthPair>

    @GET("api/space/{remoteSpaceId}/config")
    suspend fun getSpaceConfig(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    @PUT("api/space/{remoteSpaceId}/config")
    suspend fun configureSpace(
        @Body config: ConfigureSpaceRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<NetworkSpaceAuthPair>

    @PUT("api/space/{remoteSpaceId}/join")
    suspend fun join(
        @Body joinSpaceReq: JoinSpaceRequest,
        @Path("remoteSpaceId") spaceID: Long
    ): ApiResult<Nothing>

    @DELETE("api/space/{remoteSpaceId}/kickall")
    suspend fun kickAllUsers(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    @DELETE("api/space/{remoteSpaceId}/quit")
    suspend fun quitSpace(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    @POST("api/space/create")
    suspend fun createSpace(@Body createSpaceReq: CreateSpaceRequest): ApiResult<Long>

    @GET("api/space")
    suspend fun getAll(): ApiResult<List<NetworkSpace>>


}