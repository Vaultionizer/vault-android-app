package com.vaultionizer.vaultapp.data.model.rf

data class Folder(
        override val type: Type,
        override val name: String,

        val createdAt: String?,
        val content: List<Element>?
) : Element()