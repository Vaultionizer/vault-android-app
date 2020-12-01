package com.vaultionizer.vaultapp.data.model.rest.refFile

import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.Type

data class NetworkFolder(
    override val type: Type = Type.FOLDER,
    override val name: String,
    override val id: Long,

    val createdAt: Long?,
    var content: MutableList<NetworkElement>?
) : NetworkElement()