package com.vaultionizer.vaultapp.data.model.rest.request

data class ConfigureSpaceRequest(
    val usersWriteAccess: Boolean,
    val usersAuthAccess: Boolean,
    val sharedSpace: Boolean
)