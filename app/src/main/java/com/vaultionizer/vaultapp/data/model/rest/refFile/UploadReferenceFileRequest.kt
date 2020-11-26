package com.vaultionizer.vaultapp.data.model.rest.refFile

data class UploadReferenceFileRequest(
    val content: String,
    val spaceID: Long
)