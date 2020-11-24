package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.rf.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.rf.ReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.rf.UploadReferenceFileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReferenceFileService {

    @POST("api/refFile/read")
    suspend fun downloadReferenceFile(@Body downloadReq: DownloadReferenceFileRequest): ApiResult<ReferenceFile>

    @POST("api/refFile/update")
    suspend fun uploadReferenceFile(@Body uploadReq: UploadReferenceFileRequest): ApiResult<ReferenceFile>

}