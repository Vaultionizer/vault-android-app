package com.vaultionizer.vaultapp.ui.register

/**
 * Authentication result : success (user details) or error message.
 */
data class RegisterResult(
        val success: RegisteredUserView? = null,
        val error: Int? = null
)