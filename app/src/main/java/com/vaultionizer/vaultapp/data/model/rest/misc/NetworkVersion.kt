package com.vaultionizer.vaultapp.data.model.rest.misc

data class NetworkVersion(
    val hasAuthKey : Boolean,
    val version: String,
    val maintainer: String
)