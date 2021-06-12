package com.vaultionizer.vaultapp.util

import java.util.*

val <T : Enum<T>> Enum<T>.readableName: String
    get() = "${name[0].uppercaseChar()}${name.substring(1).lowercase(Locale.getDefault())}"