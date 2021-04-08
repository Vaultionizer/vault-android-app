package com.vaultionizer.vaultapp.ui.auth.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val error: String? = null
)