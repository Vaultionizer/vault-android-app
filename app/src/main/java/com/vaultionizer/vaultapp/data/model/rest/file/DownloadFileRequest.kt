package com.vaultionizer.vaultapp.data.model.rest.file

data class DownloadFileRequest(
    val saveIndex : Long,
    val spaceID : Long
)