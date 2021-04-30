package com.vaultionizer.vaultapp.cryptography

import com.google.common.truth.Truth.assertThat
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import org.junit.Before
import org.junit.Test

class CryptographyTest {

    private val testingSpaceID = 1337133713371337
    private val message = "This is a secret!"
    private val password = "00000"

    @Before
    fun cleanup() {
        Cryptography().deleteKey(testingSpaceID)
    }

    fun createSingleUserKey_AES_GCM_NoPadding() {
        Cryptography().createSingleUserKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding
        )
    }

    @Test
    fun testCryptography_keyDoesExist() {
        createSingleUserKey_AES_GCM_NoPadding()
        val result = Cryptography().existsKey(testingSpaceID)

        assertThat(result).isTrue()
    }

    @Test
    fun testCryptography_deleteKeySuccessful() {
        createSingleUserKey_AES_GCM_NoPadding()
        val result = Cryptography().deleteKey(testingSpaceID)

        assertThat(result).isTrue()
    }

    @Test
    fun testCryptography_keyDoesNotExist() {
        val result = Cryptography().existsKey(testingSpaceID)

        assertThat(result).isFalse()
    }

    @Test
    fun testCryptography_deSalterWorks() {
        val transferBytes = Cryptography().createSharedKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding,
            "0"
        )
        val saltIvcipher = Cryptography().desalter(transferBytes)

        assertThat(saltIvcipher.salt.size).isEqualTo(16)

    }

    @Test
    fun testCryptography_importExport() {
        // Perspective Person A
        val transferBytes = Cryptography().createSharedKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding,
            password
        )

        val secretKey = Cryptography().getKey(testingSpaceID)
        val ivCipher = AesGcmNopadding().encrypt(secretKey, message.toByteArray())
        Cryptography().deleteKey(testingSpaceID)

        // Perspective Person B

        Cryptography().importKey(testingSpaceID, transferBytes, password)
        val result = Cryptography().decryptData(Cryptography().getKey(testingSpaceID), ivCipher.iv, ivCipher.cipher)

        assertThat(result).isEqualTo("This is a secret!".toByteArray())
    }


}