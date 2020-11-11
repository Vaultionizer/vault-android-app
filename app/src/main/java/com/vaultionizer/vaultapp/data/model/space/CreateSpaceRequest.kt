package com.vaultionizer.vaultapp.data.model.space

data class CreateSpaceRequest(
    val authKey: String,
    val isPrivate: Boolean,
    val referenceFile: String
)