package com.vaultionizer.vaultapp.cryptography.model

import javax.crypto.SecretKey

data class KeySalt(
    val key: SecretKey,
    val salt: ByteArray
)
