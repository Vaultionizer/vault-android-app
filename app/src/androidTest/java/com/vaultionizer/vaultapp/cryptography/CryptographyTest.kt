package com.vaultionizer.vaultapp.cryptography

import com.google.common.truth.Truth.assertThat
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.cryptography.model.Password
import org.junit.Before
import org.junit.Test
import javax.crypto.AEADBadTagException

class CryptographyTest {

    private val testingSpaceID = 1337133713371337
    private val message = "This is a secret!."
    private val password = Password("00000".toByteArray(Charsets.UTF_8))

    @Before
    fun cleanup() {
        Cryptography.deleteKey(testingSpaceID)
    }

    fun createSingleUserKey_AES_GCM_NoPadding() {
        Cryptography.createSingleUserKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding
        )
    }

    @Test
    fun testCryptography_keyDoesExist() {
        createSingleUserKey_AES_GCM_NoPadding()
        val result = Cryptography.existsKey(testingSpaceID)

        assertThat(result).isTrue()
    }

    @Test
    fun testCryptography_deleteKeySuccessful() {
        createSingleUserKey_AES_GCM_NoPadding()
        val result = Cryptography.deleteKey(testingSpaceID)

        assertThat(result).isTrue()
    }

    @Test
    fun testCryptography_keyDoesNotExist() {
        val result = Cryptography.existsKey(testingSpaceID)

        assertThat(result).isFalse()
    }

    @Test
    fun testCryptography_deSalterWorks() {
        val transferBytes = Cryptography.createSharedKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding,
            password
        )
        val saltIvcipher = Cryptography.desalter(transferBytes)

        assertThat(saltIvcipher.salt.size).isEqualTo(29)

    }

    @Test
    fun testCryptography_importExport() {
        val transferBytes = Cryptography.createSharedKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding,
            password
        )
        val secretKey = Cryptography.getKey(testingSpaceID)
        val ivCipher = AesGcmNopadding().encrypt(secretKey, message.toByteArray())
        Cryptography.deleteKey(testingSpaceID)

        Cryptography.importKey(testingSpaceID, transferBytes, password)
        val result = Cryptography.decryptData(
            Cryptography.getKey(testingSpaceID),
            ivCipher.iv,
            ivCipher.cipher
        )

        assertThat(result).isEqualTo(message.toByteArray())
    }

    @Test(expected = AEADBadTagException::class)
    fun testCryptography_importExportFalsePassword() {
        val transferBytes = Cryptography.createSharedKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding,
            password
        )
        Cryptography.importKey(testingSpaceID, transferBytes, Password(password.pwd+0))
    }


}