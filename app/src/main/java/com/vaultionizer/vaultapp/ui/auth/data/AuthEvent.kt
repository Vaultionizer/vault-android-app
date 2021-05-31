package com.vaultionizer.vaultapp.ui.auth.data

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion

sealed class AuthEvent {
    data class HostValidation(
        val isLoading: Boolean = false,
        val error: String? = null,
        val version: NetworkVersion? = null
    ) : AuthEvent()

    data class UserDataValidation(
        val usernameError: String? = null,
        val passwordError: String? = null,
        val isDataValid: Boolean = false
    ) : AuthEvent()

    data class LoginValidation(
        val isLoading: Boolean? = false,
        val error: String? = null
    ) : AuthEvent()

    data class AuthKeyValidation(
        val error: String? = null
    ) : AuthEvent()
}