package com.vaultionizer.vaultapp.data.model.pc

data class PCPair constructor(
    var id: Int,
    val key: String,
    val value: String,
    var categoryId: Int?
)