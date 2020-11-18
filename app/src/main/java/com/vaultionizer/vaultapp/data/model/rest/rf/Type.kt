package com.vaultionizer.vaultapp.data.model.rest.rf

import com.google.gson.annotations.SerializedName

enum class Type {

    @SerializedName("file")
    FILE,

    @SerializedName("directory")
    FOLDER

}