package com.vaultionizer.vaultapp.data.model.rest.request

data class DeleteFileRequest(
    val saveIndex : Long,
    val spaceID : Long
)