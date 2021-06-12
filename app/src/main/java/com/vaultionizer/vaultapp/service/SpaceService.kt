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

    /**
     * Deletes a specific space from the remote server.
     *
     * @param spaceID   Id of the space on the remote server.
     * @return          Nothing.
     */
    @DELETE("api/space/{remoteSpaceId}")
    suspend fun deleteSpace(@Path("remoteSpaceId") spaceID: Long): ApiResult<Nothing>

    /**
     * Requests an Auth-Key for a specific space.
     * This key can be shared to others which can join the space referenced by [spaceID].
     *
     * @param spaceID   Id of the space on the remote server.
     * @return          An object containing the auth-key.
     */
    @GET("api/space/{remoteSpaceId}/authkey")
    suspend fun getAuthKey(@Path("remoteSpaceId") spaceID: Long): ApiResult<NetworkSpaceAuthPair>

    /**
     * Changes the auth key of a specific space on the remote server.
     *
     * @param authKey       The new auth key.
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              An object containing the new auth-key.
     */
    @PUT("api/space/{remoteSpaceId}/authkey")
    suspend fun changeAuthKey(
        @Body authKey: ChangeAuthKeyRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<NetworkSpaceAuthPair>

    /**
     * Queries the space permissions configuration of a specific space from the remote server.
     *
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              An object containing the auth-key to join the space.
     */
    @GET("api/space/{remoteSpaceId}/config")
    suspend fun getSpaceConfig(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    /**
     * Changes the permissing configuration of a specific space.
     *
     * @param config            An object containing the new configuration.
     * @param remoteSpaceId     Id of the space on the remote server.
     * @return                  An object containing the new auth-key to join the space.
     */
    @PUT("api/space/{remoteSpaceId}/config")
    suspend fun configureSpace(
        @Body config: ConfigureSpaceRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<NetworkSpaceAuthPair>

    /**
     * Adds the user to the space referenced by [spaceID].
     *
     * @param joinSpaceReq  An object containing the auth key for joining the specific space.
     * @param spaceID       Id of the space on the remote server.
     * @return              Nothing.
     */
    @PUT("api/space/{remoteSpaceId}/join")
    suspend fun join(
        @Body joinSpaceReq: JoinSpaceRequest,
        @Path("remoteSpaceId") spaceID: Long
    ): ApiResult<Unit>

    /**
     * Kicks a other users from the space.
     *
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              An object containing the existing auth-key.
     */
    @DELETE("api/space/{remoteSpaceId}/kickall")
    suspend fun kickAllUsers(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    /**
     * Removes the current user from the space referenced by [remoteSpaceId].
     *
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              An object containing the auth-key in case the user decides to re-join
     *                      the space.
     */
    @DELETE("api/space/{remoteSpaceId}/quit")
    suspend fun quitSpace(@Path("remoteSpaceId") remoteSpaceId: Long): ApiResult<NetworkSpaceAuthPair>

    /**
     * Creates a space on remote server.
     * The request should also contain an initial reference file.
     *
     * @param createSpaceReq    The initial configuration of the space.
     * @return                  The remote Id of the newly created space on the server.
     */
    @POST("api/space/create")
    suspend fun createSpace(@Body createSpaceReq: CreateSpaceRequest): ApiResult<Long>

    /**
     * Queries all spaces for the current user on the remote server.
     *
     * @return  A list of all spaces the user either created or joined.
     */
    @GET("api/space")
    suspend fun getAll(): ApiResult<List<NetworkSpace>>


}