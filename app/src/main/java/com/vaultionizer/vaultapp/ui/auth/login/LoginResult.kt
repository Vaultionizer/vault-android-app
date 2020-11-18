package com.vaultionizer.vaultapp.ui.auth.login

import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val error: String? = null
)