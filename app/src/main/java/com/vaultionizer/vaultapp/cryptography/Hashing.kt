package com.vaultionizer.vaultapp.cryptography

import java.security.MessageDigest

class Hashing {
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun sha256(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    fun sha512(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun sha512(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-512")
        return md.digest(bytes)
    }
}