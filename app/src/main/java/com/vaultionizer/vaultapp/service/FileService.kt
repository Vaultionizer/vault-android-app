package com.vaultionizer.vaultapp.service

import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import retrofit2.http.*

interface FileService {

    /**
     * Reserves the next available, unique file id from the server.
     * The upload itself is done via a WebSocket in a separate step.
     *
     * @param uploadFileReq The request body.
     * @param remoteSpaceId Id of the space on the remote server.
     * @return              The saveIndex/remoteFileId of the newly requested file.
     */
    @POST("api/file/{remoteSpaceId}/upload")
    suspend fun uploadFile(
        @Body uploadFileReq: UploadFileRequest,
        @Path("remoteSpaceId") remoteSpaceId: Long
    ): ApiResult<Long>


    /**
     * Requests the download of a specific file.
     * The file itself is sent to a specific STOMP topic.
     *
     * @param remoteSpaceId     Id of the space on the remote server.
     * @param remoteSaveIndex   Id of the file on the remote server.
     */
    @PUT("api/file/{remoteSpaceId}/{remoteSaveIndex}/download")
    suspend fun downloadFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )

    /**
     * Deletes a file from the remote server.
     *
     * @param remoteSpaceId     Id of the space on the remote server.
     * @param remoteSaveIndex   Id of the file on the remote server.
     */
    @DELETE("api/file/{remoteSpaceId}/{remoteSaveIndex}")
    suspend fun deleteFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )

    /**
     * Notifies the server that the client wants to update the content of a specific file.
     * The new content is sent via a specific STOMP topic to the server in a separate step..
     *
     * @param remoteSpaceId     Id of the space on the remote server.
     * @param remoteSaveIndex   Id of the file on the remote server.
     */
    @POST("api/file/{remoteSpaceId}/{remoteSaveIndex}/update")
    suspend fun updateFile(
        @Path("remoteSpaceId") remoteSpaceId: Long,
        @Path("remoteSaveIndex") remoteSaveIndex: Long
    )
}