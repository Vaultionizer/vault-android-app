package com.vaultionizer.vaultapp.data.model.domain

data class VNUser(
    val id: Long,
    val remoteId: Long,
    val username: String,
    val endpoint: String,
    val lastLogin: Long
)
