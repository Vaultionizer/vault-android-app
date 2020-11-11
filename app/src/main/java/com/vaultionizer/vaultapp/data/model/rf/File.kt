package com.vaultionizer.vaultapp.data.model.rf

data class File(
        override val type: Type,
        override val name: String,
        val crc: String,
        val size: Long,
        val id: String,
        val createdAt: String,
        val updatedAt: String
) : Element()