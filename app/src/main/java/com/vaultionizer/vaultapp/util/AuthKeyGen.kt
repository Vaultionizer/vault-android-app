package com.vaultionizer.vaultapp.util

import java.util.*

class AuthKeyGen {
    fun generateAuthKey(): String {
        return UUID.randomUUID().toString()
    }
}