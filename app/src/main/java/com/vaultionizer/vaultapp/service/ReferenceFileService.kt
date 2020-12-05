package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.UploadReferenceFileRequest
import retrofit2.http.*

interface ReferenceFileService {

    @POST("api/refFile/read")
    suspend fun downloadReferenceFile(@Body downloadReq: DownloadReferenceFileRequest): ApiResult<NetworkReferenceFile>

    @POST("api/refFile/update/")
    suspend fun uploadReferenceFile(@Body uploadReq: UploadReferenceFileRequest): ApiResult<NetworkReferenceFile>

}