package com.vaultionizer.vaultapp.data.model.rest.rf

data class Folder(
    override val type: Type = Type.FOLDER,
    override val name: String,

    val createdAt: String?,
    var content: MutableList<Element>?
) : Element()