package com.vaultionizer.vaultapp.cryptography.dataclasses

data class Hash(
    val hash : ByteArray
){
    override fun toString(): String = hash.toString(Charsets.UTF_8)
}
