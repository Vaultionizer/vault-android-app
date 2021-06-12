package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.request.UploadReferenceFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReferenceFileService {

    /**
     * Downloads the reference file of specific space.
     *
     * @param remoteSpaceId     Id of the space on the remote server.
     * @param downloadRequest   An object containing the last time this reference was read.
     * @return                  The reference file already converted to an object.
     */
    @POST("api/refFile/{remoteSpaceId}/read")
    suspend fun downloadReferenceFile( // TODO add current timestamp
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Body downloadRequest: DownloadReferenceFileRequest
    ): ApiResult<NetworkReferenceFile>

    /**
     * Uploads the reference file of a specific space.
     *
     * @param uploadRequest An object containing the raw json representation of an reference file.
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              Nothing.
     */
    @PUT("api/refFile/{remoteSpaceId}/update")
    suspend fun uploadReferenceFile(
        @Body uploadRequest: UploadReferenceFileRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<Unit>

}