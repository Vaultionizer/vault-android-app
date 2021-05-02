package com.vaultionizer.vaultapp.service

interface FileExchangeService {

    suspend fun uploadFile(spaceRemoteId: Long, fileRemoteId: Long, data: ByteArray)

    suspend fun downloadFile(spaceRemoteId: Long, fileRemoteId: Long): ByteArray?

}