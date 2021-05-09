package com.vaultionizer.vaultapp.cryptography.dataclasses

data class HashSalt(
    val hash: Hash,
    val salt: Salt
)
