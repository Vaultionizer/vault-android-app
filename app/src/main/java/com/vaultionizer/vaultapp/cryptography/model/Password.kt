package com.vaultionizer.vaultapp.cryptography.model

data class Password(
    val pwd : ByteArray
) {
    override fun toString(): String = pwd.toString(Charsets.UTF_8)
}