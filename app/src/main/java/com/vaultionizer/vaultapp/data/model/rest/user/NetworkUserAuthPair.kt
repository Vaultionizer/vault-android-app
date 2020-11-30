package com.vaultionizer.vaultapp.data.model.rest.user

data class NetworkUserAuthPair (
    val userID: Long,
    val sessionKey: String,
    val websocketToken: String
)