package com.vaultionizer.vaultapp.cryptography.model

data class SharedKeyOutput(
    val salt: ByteArray,
    val iv: ByteArray,
    val cipher: ByteArray
)
