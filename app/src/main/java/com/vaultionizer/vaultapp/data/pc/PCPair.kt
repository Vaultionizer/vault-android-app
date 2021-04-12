package com.vaultionizer.vaultapp.data.pc

data class PCPair constructor(
    var id: Int,
    val key: String,
    val value: String,
    val categoryId: Int?
)