package com.vaultionizer.vaultapp.data.model.rest.rf

data class NetworkFile(
    override val type: Type = Type.FILE,
    override val name: String,
    override val id: Long,
    val crc: String,
    val size: Long,
    val createdAt: Long,
    val updatedAt: Long
) : NetworkElement()