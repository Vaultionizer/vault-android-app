package com.vaultionizer.vaultapp.data.model.rest.request

data class CreateUserRequest(
    val key: String,
    val refFile: String,
    val username: String
)