package com.vaultionizer.vaultapp.data.source

import com.google.gson.JsonObject
import com.vaultionizer.vaultapp.data.model.misc.Version
import retrofit2.Call
import retrofit2.http.GET

interface MiscService {

    @GET("misc/version")
    fun getVersionInfo(): Call<Version>

    @GET("misc/version")
    fun echoCall(): Call<JsonObject>

}