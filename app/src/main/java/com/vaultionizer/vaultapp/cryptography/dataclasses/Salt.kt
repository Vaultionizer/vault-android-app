package com.vaultionizer.vaultapp.cryptography.dataclasses

data class Salt(
    val salt : ByteArray
){
    override fun toString(): String = salt.toString(Charsets.UTF_8)
}
