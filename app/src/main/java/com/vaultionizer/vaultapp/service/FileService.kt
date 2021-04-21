package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.DeleteFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface FileService {

    @POST("api/file/upload")
    suspend fun uploadFile(@Body uploadFileReq: UploadFileRequest): ApiResult<Long>

    @PUT("api/file/download")
    @Headers("Content-Type: application/json")
    suspend fun downloadFile(@Body downloadFileReq: DownloadFileRequest)

    @PUT("api/file/delete")                                                 //TODO will be changed to delete in the future 11/26/2020
    suspend fun deleteFile(@Body deleteFileReq: DeleteFileRequest)

}