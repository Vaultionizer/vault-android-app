package com.vaultionizer.vaultapp.cryptography

import org.junit.Test
import com.google.common.truth.Truth.assertThat

import org.junit.Assert.*

class HashingTest {

    @Test
    fun sha256() {
        val result = Hashing().sha256("Hallo1234567890")
        assertThat(result).isEqualTo("5f979c8e8f6f879420e932174546ca7ac47c50df9f15c7a3178fef08c256d61d")
    }
}