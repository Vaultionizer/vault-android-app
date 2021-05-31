package com.vaultionizer.vaultapp.data.model.rest.request

data class ValidateAuthKeyRequest(
    val serverAuthKey: String,
    val serverUser: String
)
