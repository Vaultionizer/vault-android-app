package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.request.ValidateAuthKeyRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MiscService {

    @GET("api/misc/version")
    suspend fun getVersionInfo(): ApiResult<NetworkVersion>

    @POST("api/misc/checkAuthenticated")
    suspend fun validateAuthKey(@Body validateAuthKeyRequest: ValidateAuthKeyRequest): ApiResult<Nothing>

}