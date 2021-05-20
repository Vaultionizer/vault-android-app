package com.vaultionizer.vaultapp.cryptography

import com.google.common.truth.Truth
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import org.junit.Before
import org.junit.Test
import java.nio.charset.Charset
import java.util.*
import javax.crypto.spec.SecretKeySpec

class AesCbcNopaddingTest{
    private val testingSpaceID = 1337133713371337

    @Before
    fun cleanup() {
        Cryptography.deleteKey(testingSpaceID)
    }

    @Test
    fun testCryptography_en_de_crypt_AES_CBC_NoPadding(){

        val message = ByteArray(16) // length is bounded by 7
        Random().nextBytes(message)

        val secretKey = SecretKeySpec(message, "AES")
        val encrypt =  AesCbcNopadding().encrypt(secretKey, message)
        val result = AesCbcNopadding().decrypt(secretKey, encrypt.iv, encrypt.cipher)

        Truth.assertThat(result).isEqualTo(message)
    }

    @Test
    fun testCryptography_en_de_crypt_AES_CBC_NoPadding_from_Keystore(){
        Cryptography.deleteKey(testingSpaceID)
        Cryptography.createSingleUserKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.CBC,
            CryptoPadding.NoPadding
        )
        val message = ByteArray(16)

        Random().nextBytes(message)

        val secretKey = Cryptography.getKey(testingSpaceID)
        val encrypt =  AesCbcNopadding().encrypt(secretKey, message)
        val result = AesCbcNopadding().decrypt(secretKey, encrypt.iv, encrypt.cipher)

        Truth.assertThat(result).isEqualTo(message)
    }
}