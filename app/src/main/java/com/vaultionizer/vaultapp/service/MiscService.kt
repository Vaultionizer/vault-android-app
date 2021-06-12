package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.request.ValidateAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MiscService {

    /**
     * Queries information about the servers Vaultionizer installation.
     *
     * @return  Information about the installation.
     */
    @GET("api/misc/version")
    suspend fun getVersionInfo(): ApiResult<NetworkVersion>

    @POST("api/misc/checkAuthenticated")
    suspend fun validateAuthKey(@Body validateAuthKeyRequest: ValidateAuthKeyRequest): ApiResult<Nothing>

}