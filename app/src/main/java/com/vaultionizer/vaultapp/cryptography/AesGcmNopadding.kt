package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.vaultionizer.vaultapp.cryptography.model.IvCipher
import com.vaultionizer.vaultapp.util.Constants
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AesGcmNopadding : CryptoClass() {

    companion object {
        const val TAG_LENGTH = 16
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val BLOCK_MODE_IV_SIZE = 12
    }

    override fun generateSingleUserKey(keystoreAlias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            Constants.VN_KEYSTORE_PROVIDER
        )
        val kgps = KeyGenParameterSpec.Builder(
            keystoreAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .setKeySize(256)
            .build()
        keyGenerator.init(kgps)
        keyGenerator.generateKey()
    }

    override fun encrypt(key: SecretKey, message: ByteArray): IvCipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv.copyOf()
        val cipherText = cipher.doFinal(message)
        return IvCipher(iv, cipherText)
    }

    override fun decrypt(key: SecretKey, iv: ByteArray, message: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(message)
    }

    override fun dewrapper(warp: ByteArray): IvCipher {
        val iv: ByteArray = warp.sliceArray(0 until BLOCK_MODE_IV_SIZE)
        val cipherText: ByteArray = warp.sliceArray(BLOCK_MODE_IV_SIZE until warp.size)

        return IvCipher(iv, cipherText)
    }

    override fun addKeyToKeyStore(secretKey: SecretKey, keystoreAlias: String) {
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.setEntry(
            keystoreAlias,
            KeyStore.SecretKeyEntry(secretKey),
            KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
    }
}