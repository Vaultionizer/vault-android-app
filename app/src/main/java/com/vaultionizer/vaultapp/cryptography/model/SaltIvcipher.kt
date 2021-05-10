package com.vaultionizer.vaultapp.cryptography.model

data class SaltIvcipher(
    val salt: ByteArray,
    val ivcipher: IvCipher
)
