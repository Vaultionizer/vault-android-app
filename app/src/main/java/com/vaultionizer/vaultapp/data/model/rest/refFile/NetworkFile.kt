package com.vaultionizer.vaultapp.data.model.rest.refFile

import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.Type

data class NetworkFile(
    override val type: Type = Type.FILE,
    override val name: String,
    override val id: Long,
    val crc: String,
    val size: Long,
    val createdAt: Long,
    val updatedAt: Long
) : NetworkElement()