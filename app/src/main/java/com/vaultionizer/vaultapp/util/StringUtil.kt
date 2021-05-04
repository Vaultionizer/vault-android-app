package com.vaultionizer.vaultapp.util

val <T : Enum<T>> Enum<T>.readableName: String
    get() = "${name[0].toUpperCase()}${name.substring(1).toLowerCase()}"