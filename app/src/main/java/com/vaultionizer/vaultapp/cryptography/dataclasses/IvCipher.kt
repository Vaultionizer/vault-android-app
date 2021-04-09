package com.vaultionizer.vaultapp.cryptography.dataclasses

data class IvCipher(
    val iv: ByteArray,
    val cipher: ByteArray
)
