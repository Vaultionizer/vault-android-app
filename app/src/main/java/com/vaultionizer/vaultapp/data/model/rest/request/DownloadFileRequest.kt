package com.vaultionizer.vaultapp.data.model.rest.request

data class DownloadFileRequest(
    val saveIndex: Long,
    val spaceID: Long
)