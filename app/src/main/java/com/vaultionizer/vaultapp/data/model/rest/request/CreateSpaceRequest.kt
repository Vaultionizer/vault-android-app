package com.vaultionizer.vaultapp.data.model.rest.request

data class CreateSpaceRequest(
    val authKey: String,
    val isPrivate: Boolean,
    val referenceFile: String,
    val usersAuthAccess: Boolean,
    val usersWriteAccess: Boolean
)