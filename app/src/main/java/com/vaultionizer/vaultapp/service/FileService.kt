package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.*

interface FileService {

    @POST("api/file/{remoteSpaceId}/upload")
    suspend fun uploadFile(
        @Body uploadFileReq: UploadFileRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<Long>

    @PUT("api/file/{remoteSpaceId}/{remoteSaveIndex}/download")
    suspend fun downloadFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )

    @DELETE("api/file/{remoteSpaceId}/{remoteSaveIndex}")
    suspend fun deleteFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )

    @POST("api/file/{remoteSpaceId}/{remoteSaveIndex}/update")
    suspend fun updateFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )
}