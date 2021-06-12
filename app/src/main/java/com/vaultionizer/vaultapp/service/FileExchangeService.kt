package com.vaultionizer.vaultapp.service

interface FileExchangeService {

    /**
     * Uploads a file via STOMP to the remote server.
     * The content of [data] will be encoded with Base64 NO_WRAP.
     *
     * @param spaceRemoteId     Id of the space on the remote server.
     * @param fileRemoteId      Id of the file on the remote server.
     * @param data              The data that is uploaded.
     */
    suspend fun uploadFile(spaceRemoteId: Long, fileRemoteId: Long, data: ByteArray)

    /**
     * Downloads a file via STOMP from the remote server.
     *
     * @param spaceRemoteId     Id of the space on the remote server
     * @param fileRemoteId      Id of the file on the remote server.
     * @return                  Returns the decoded, but still encrypted data as ByteArray.
     */
    suspend fun downloadFile(spaceRemoteId: Long, fileRemoteId: Long): ByteArray?

}