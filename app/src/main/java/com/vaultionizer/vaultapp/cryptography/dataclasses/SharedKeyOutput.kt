package com.vaultionizer.vaultapp.cryptography.dataclasses

data class SharedKeyOutput(
    val salt: ByteArray,
    val iv: ByteArray,
    val cipher: ByteArray
)
