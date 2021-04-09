package com.vaultionizer.vaultapp.cryptography.dataclasses

data class SharedKeyOutput(
    val salt: ByteArray,
    val cipher: ByteArray,
    val iv: ByteArray
)
