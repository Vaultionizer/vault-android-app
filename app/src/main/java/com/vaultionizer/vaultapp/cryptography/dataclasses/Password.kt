package com.vaultionizer.vaultapp.cryptography.dataclasses

data class Password(
    val pwd : ByteArray
) {
    override fun toString(): String = pwd.toString(Charsets.UTF_8)
}