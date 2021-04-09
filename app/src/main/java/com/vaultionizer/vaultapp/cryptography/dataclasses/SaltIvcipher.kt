package com.vaultionizer.vaultapp.cryptography.dataclasses

data class SaltIvcipher(
    val salt: ByteArray,
    val ivcipher: IvCipher
)
