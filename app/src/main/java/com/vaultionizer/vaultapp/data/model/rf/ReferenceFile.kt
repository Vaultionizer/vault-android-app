package com.vaultionizer.vaultapp.data.model.rf

import com.google.gson.annotations.SerializedName

data class ReferenceFile(
        val version: Int,
        @SerializedName("files") val elements: Array<Element>
)