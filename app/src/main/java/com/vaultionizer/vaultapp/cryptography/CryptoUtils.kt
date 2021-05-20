package com.vaultionizer.vaultapp.cryptography

import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.cryptography.model.Password
import java.util.*

object CryptoUtils {
    fun generateKeyForSingleUserSpace(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding
    ) {
        Cryptography.createSingleUserKey(spaceID, cryptoType, cryptoMode, cryptoPadding)
    }

    fun generateKeyForSharedSpace(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding,
        password: String
    ): ByteArray {
        return Cryptography.createSharedKey(spaceID, cryptoType, cryptoMode, cryptoPadding, Password(password.toByteArray(Charsets.UTF_8)))
    }

    fun importKeyForSharedSpace(spaceID: Long, transferBytes: ByteArray, password: String): Boolean {
        return Cryptography.importKey(spaceID, transferBytes, Password(password.toByteArray(Charsets.UTF_8)))
    }

    fun deleteKey(spaceID: Long): Boolean {
        return Cryptography.deleteKey(spaceID)
    }

    fun listKeys(): Enumeration<String> {
        return Cryptography.listKeys()
    }

    fun existsKey(spaceID: Long): Boolean {
        return Cryptography.existsKey(spaceID)
    }

    fun encryptData(spaceID: Long, bytes: ByteArray): ByteArray {
        return Cryptography.encryptor(spaceID, bytes)
    }

    fun decryptData(spaceID: Long, bytes: ByteArray): ByteArray {
        return Cryptography.decrytor(spaceID, bytes)
    }



}