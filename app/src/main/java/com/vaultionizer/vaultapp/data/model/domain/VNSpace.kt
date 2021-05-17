package com.vaultionizer.vaultapp.data.model.domain

import com.vaultionizer.vaultapp.cryptography.Cryptography

class VNSpace(
    val id: Long,
    val remoteId: Long,
    val userId: Long,
    val name: String?,
    val lastAccess: Long,
    val owner: Boolean,
    val lastSuccessfulFetch: Long
) {

    val isKeyAvailable
        get() = Cryptography().existsKey(id)

}