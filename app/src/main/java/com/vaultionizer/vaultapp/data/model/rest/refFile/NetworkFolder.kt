package com.vaultionizer.vaultapp.data.model.rest.refFile

data class NetworkFolder(
    override val type: Type = Type.FOLDER,
    override val name: String,
    override val id: Long,

    val createdAt: Long?,
    var content: MutableList<NetworkElement>?
) : NetworkElement()