package com.vaultionizer.vaultapp.data.model.rest.refFile

data class File(
    override val type: Type = Type.FILE,
    override val name: String,
    val crc: String,
    val size: Long,
    val id: Long,
    val createdAt: String,
    val updatedAt: String
) : Element()