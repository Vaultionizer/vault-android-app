package com.vaultionizer.vaultapp.data.model.rest.request

data class JoinSpaceRequest(
    val spaceID: Long,
    val authKey: String
)