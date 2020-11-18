package com.vaultionizer.vaultapp.service

import com.google.gson.JsonObject
import com.vaultionizer.vaultapp.data.model.rest.misc.Version
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.Call
import retrofit2.http.GET

interface MiscService {

    @GET("misc/version")
    suspend fun getVersionInfo(): ApiResult<Version>

    @GET("misc/version")
    fun echoCall(): Call<JsonObject>
}