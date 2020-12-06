package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.vaultionizer.vaultapp.util.Constants
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class AesGcmNopadding : CryptoClass(){

    companion object{
        const val TAG_LENGTH = 16
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val BLOCK_MODE_IV_SIZE = 12
    }


    override fun generateKey(keystoreAlias : String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Constants.VN_KEYSTORE_PROVIDER)
        val kgps = KeyGenParameterSpec.Builder(keystoreAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .setKeySize(256)
                .build()
        keyGenerator.init(kgps)
        return keyGenerator.generateKey()
    }

    override fun encrypt(key: SecretKey, message: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv.copyOf()
        val cipherText = cipher.doFinal(message)
        return Pair(iv, cipherText)
    }

    override fun decrypt(key: SecretKey, iv: ByteArray, message: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(message)
    }

    override fun dewrapper(warp : ByteArray) : Pair<ByteArray,ByteArray>{
        val iv : ByteArray = warp.sliceArray(0 until BLOCK_MODE_IV_SIZE)
        val cipherText : ByteArray = warp.sliceArray(BLOCK_MODE_IV_SIZE until warp.size)

        return Pair(iv, cipherText)
    }

    /*
    vaultionizer_{VaultID}
    TODO Multiple and TTL of Keys
     */
}


