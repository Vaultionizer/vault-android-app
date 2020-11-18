package com.vaultionizer.vaultapp.data.model.rest.space

data class CreateSpaceRequest(
    val authKey: String,
    val isPrivate: Boolean,
    val referenceFile: String
)