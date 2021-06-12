package com.vaultionizer.vaultapp.cryptography.dataclasses

import javax.crypto.SecretKey

data class KeySalt(
    val key: SecretKey,
    val salt: ByteArray
)
