package com.vaultionizer.vaultapp.cryptography.model

data class Hash(
    val hash : ByteArray
){
    override fun toString(): String = hash.toString(Charsets.UTF_8)
    fun toByteArray(): ByteArray = hash
}
