package com.vaultionizer.vaultapp.cryptography


import com.google.common.truth.Truth.assertThat
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import org.junit.Before
import org.junit.Test
import java.nio.charset.Charset
import java.util.*
import javax.crypto.spec.SecretKeySpec

class AesGcmNopaddingTest{

    private val testingSpaceID = 1337133713371337

    @Before
    fun cleanup() {
        Cryptography.deleteKey(testingSpaceID)
    }

    @Test
    fun testCryptography_en_de_crypt_AES_GCM_NoPadding(){

        val array = ByteArray(16) // length is bounded by 7

        Random().nextBytes(array)

        val generatedString = String(array, Charset.forName("UTF-8"))


        val message = generatedString.toByteArray()
        val secretKey = SecretKeySpec(array, "AES")
        val encrypt =  AesGcmNopadding.encrypt(secretKey, message)
        val result = AesGcmNopadding.decrypt(secretKey, encrypt.iv, encrypt.cipher)

        assertThat(result).isEqualTo(message)
    }

    @Test
    fun testCryptography_en_de_crypt_AES_GCM_NoPadding_from_Keystore(){
        Cryptography.deleteKey(testingSpaceID)
        Cryptography.createSingleUserKey(
            testingSpaceID,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding
        )
        val array = ByteArray(16)

        Random().nextBytes(array)

        val generatedString = String(array, Charset.forName("UTF-8"))

        val message = generatedString.toByteArray()
        val secretKey = Cryptography.getKey(testingSpaceID)
        val encrypt =  AesGcmNopadding.encrypt(secretKey, message)
        val result = AesGcmNopadding.decrypt(secretKey, encrypt.iv, encrypt.cipher)

        assertThat(result).isEqualTo(message)
    }
}