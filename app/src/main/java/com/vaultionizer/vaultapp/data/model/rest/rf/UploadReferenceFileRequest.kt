package com.vaultionizer.vaultapp.data.model.rest.rf

data class UploadReferenceFileRequest(
    val content: String,
    val spaceID: Long
)