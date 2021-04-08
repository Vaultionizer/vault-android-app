package com.vaultionizer.vaultapp.cryptography

import javax.crypto.SecretKey

abstract class CryptoClass {
    abstract fun generateKey(keystoreAlias: String): SecretKey
    abstract fun encrypt(key: SecretKey, message: ByteArray): Pair<ByteArray, ByteArray>
    abstract fun decrypt(key: SecretKey, iv: ByteArray, message: ByteArray): ByteArray
    abstract fun dewrapper(warp: ByteArray): Pair<ByteArray, ByteArray>

}