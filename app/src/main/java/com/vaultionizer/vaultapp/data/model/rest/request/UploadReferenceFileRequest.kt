package com.vaultionizer.vaultapp.data.model.rest.request

data class UploadReferenceFileRequest(
    val content: String,
    val spaceID: Long
)