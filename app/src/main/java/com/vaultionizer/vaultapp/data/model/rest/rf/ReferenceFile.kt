package com.vaultionizer.vaultapp.data.model.rest.rf

import com.google.gson.annotations.SerializedName

data class ReferenceFile(
        val version: Int,
        @SerializedName("files") val elements: Array<Element>
) {
        companion object {
                const val CURRENT_VERSION = 1
                val EMPTY_FILE = ReferenceFile(CURRENT_VERSION, emptyArray())
        }
}