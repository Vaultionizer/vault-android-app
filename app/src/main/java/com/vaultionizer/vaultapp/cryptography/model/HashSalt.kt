package com.vaultionizer.vaultapp.cryptography.model

data class HashSalt(
    val hash: Hash,
    val salt: Salt
) {
    fun toByteArray(): ByteArray {
        return hash.hash + salt.salt
    }
}
