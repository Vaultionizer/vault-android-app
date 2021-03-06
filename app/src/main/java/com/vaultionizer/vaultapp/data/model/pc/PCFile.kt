package com.vaultionizer.vaultapp.data.model.pc

data class PCFile constructor(
    val categories: ArrayList<PCCategory>,
    val pairs: ArrayList<PCPair>
) {
    val version: String = "v1.0"
}