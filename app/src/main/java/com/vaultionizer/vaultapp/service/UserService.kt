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

    /**
     * Creates a new user on the remote server.
     *
     * @param createUserRequest An object containing all information about the new user.
     * @return                  An object containing authentication data to make other requests
     *                          for that user.
     */
    @POST("api/user/create")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): ApiResult<NetworkUserAuthPair>

    /**
     * Logs in a user on the remote server.
     *
     * @param loginUserRequest  An object containing all relevant login information.
     * @return                  An object containing authentication data to make other requests
     *                          for that user.
     */
    @POST("api/user/login")
    suspend fun loginUser(@Body loginUserRequest: LoginUserRequest): ApiResult<NetworkUserAuthPair>

    /**
     * Deletes the current user from the remote server
     */
    @DELETE("api/user")
    suspend fun deleteUser()

    /**
     * Logs our a user. All existing session and WebSocket tokens will become invalid.
     */
    @PUT("api/user/logout")
    suspend fun logoutUser()
}