package com.vaultionizer.vaultapp.data.model.rest.rf

data class NetworkFile(
    override val type: Type = Type.FILE,
    override val name: String,
    val crc: String,
    val size: Long,
    val id: Long,
    val createdAt: String,
    val updatedAt: String
) : NetworkElement()