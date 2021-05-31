package com.vaultionizer.vaultapp.cryptography

import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.vaultionizer.vaultapp.cryptography.model.Password

class HashingTest {

    @Test
    fun sha256() {
        val result = Hashing.sha256("Hallo1234567890")
        assertThat(result).isEqualTo("5f979c8e8f6f879420e932174546ca7ac47c50df9f15c7a3178fef08c256d61d")
    }

    @Test
    fun bCryptValid() {
        val pwd = Password("TransferPassword".toByteArray(Charsets.UTF_8))
        val hashSalt = Hashing.bCryptHash(pwd)

        val result = Hashing.bCryptValidate(pwd, hashSalt)

        assertThat(result).isTrue()
    }

    @Test
    fun bCryptInvalid() {
        val pwd = Password("TransferPassword".toByteArray(Charsets.UTF_8))
        val hashSalt = Hashing.bCryptHash(pwd)

        val pwdFalse = Password("InvalidPassword".toByteArray(Charsets.UTF_8))
        val result = Hashing.bCryptValidate(pwdFalse, hashSalt)

        assertThat(result).isFalse()
    }


    @Test
    fun  test() {
        val pwd ="TransferBytes".toByteArray(Charsets.UTF_8)
        val pwdString = pwd.toString(Charsets.UTF_8)

        assertThat(pwd).isEqualTo(pwdString.toByteArray(Charsets.UTF_8))
    }
}