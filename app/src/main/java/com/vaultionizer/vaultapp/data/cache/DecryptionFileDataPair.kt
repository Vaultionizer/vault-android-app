package com.vaultionizer.vaultapp.data.cache

import com.vaultionizer.vaultapp.data.model.domain.VNFile

data class DecryptionFileDataPair(
        val file: VNFile,
        val data: ByteArray,
        var shown: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecryptionFileDataPair

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
