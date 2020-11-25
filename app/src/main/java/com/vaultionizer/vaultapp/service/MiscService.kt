package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.GET

interface MiscService {

    @GET("misc/version")
    suspend fun getVersionInfo(): ApiResult<NetworkVersion>

}