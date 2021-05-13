package com.vaultionizer.vaultapp.data.model.domain

data class VNSpace(
    val id: Long,
    val remoteId: Long,
    val userId: Long,
    val name: String?,
    val lastAccess: Long,
    val owner: Boolean,
    val lastSuccessfulFetch: Long
)
