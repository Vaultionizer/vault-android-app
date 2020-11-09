package com.vaultionizer.vaultapp.data.source

import com.vaultionizer.vaultapp.data.model.space.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SpaceService {
    @POST("space/create")
    fun createSpace(@Body createSpaceRequest: CreateSpaceRequest) : Call<Long>

    @POST("space/getall")  //TODO change to getAll later on
    fun getAll(@Body getAllRequest: GetAllRequest) : Call<Long>

    @POST("space/join")
    fun join(@Body joinRequest: JoinRequest) : Call<Long>

    @POST("space/key")
    fun getAuthKey(@Body getAuthKeyRequest: GetAuthKeyRequest) : Call<SpaceAuthPair>

}