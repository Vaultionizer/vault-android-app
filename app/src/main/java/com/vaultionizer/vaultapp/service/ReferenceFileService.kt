package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.UploadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReferenceFileService {

    @POST("api/refFile/{remoteSpaceId}/read")
    suspend fun downloadReferenceFile( // TODO add current timestamp
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<NetworkReferenceFile>

    @PUT("api/refFile/{remoteSpaceId}/update")
    suspend fun uploadReferenceFile(
        @Body uploadReq: UploadReferenceFileRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<Unit>

}