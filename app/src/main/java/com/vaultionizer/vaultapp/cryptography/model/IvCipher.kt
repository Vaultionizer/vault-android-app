package com.vaultionizer.vaultapp.cryptography.model

data class IvCipher(
    val iv: ByteArray,
    val cipher: ByteArray
)
