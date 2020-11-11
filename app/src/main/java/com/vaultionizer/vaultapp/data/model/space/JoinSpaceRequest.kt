package com.vaultionizer.vaultapp.data.model.space

data class JoinSpaceRequest(
    val spaceID: Long,
    val authKey: String
)