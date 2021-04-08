package com.vaultionizer.vaultapp.util.extension

import android.util.Base64
import java.security.MessageDigest

fun String.hash(algorithm: String): String {
    return hashString(this, algorithm)
}

fun String.hashSha256(): String {
    return hashString(this, "SHA-256")
}

fun String.hashSha512(): String {
    return hashString(this, "SHA-512")
}

private fun hashString(data: String, algorithm: String): String {
    return Base64.encodeToString(
        MessageDigest.getInstance(algorithm).digest(data.toByteArray()),
        Base64.DEFAULT
    )
}