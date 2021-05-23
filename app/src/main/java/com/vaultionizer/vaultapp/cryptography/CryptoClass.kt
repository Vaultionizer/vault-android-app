package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyProperties
import com.vaultionizer.vaultapp.cryptography.model.IvCipher
import com.vaultionizer.vaultapp.cryptography.model.Password
import com.vaultionizer.vaultapp.cryptography.model.SharedKeyOutput
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

abstract class CryptoClass {
    abstract fun generateSingleUserKey(keystoreAlias: String)

    fun generateSharedKey(keystoreAlias: String, pwd: Password): SharedKeyOutput {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()

        addKeyToKeyStore(secretKey, keystoreAlias)

        val importExportKeyAndSalt = Cryptography.generateImportExportKeyAndSalt(pwd)
        val importExportKey = importExportKeyAndSalt.key
        val importExportSalt = importExportKeyAndSalt.salt

        val pairIvCipher = AesGcmNopadding.encrypt(importExportKey, ByteArray(16) + secretKey.encoded)

        val iv = pairIvCipher.iv
        val cipher = pairIvCipher.cipher

        return SharedKeyOutput(importExportSalt, iv, cipher)
    }

    abstract fun encrypt(key: SecretKey, message: ByteArray): IvCipher
    abstract fun decrypt(key: SecretKey, iv: ByteArray, message: ByteArray): ByteArray
    abstract fun dewrapper(warp: ByteArray): IvCipher
    abstract fun addKeyToKeyStore(secretKey: SecretKey, keystoreAlias: String)
}