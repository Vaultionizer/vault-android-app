package com.vaultionizer.vaultapp.ui.auth.data

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion

sealed class AuthEvent {
    data class HostValidation(
        val isLoading: Boolean?,
        val error: Int?,
        val version: NetworkVersion?
    ) : AuthEvent()

    data class UserDataValidation(
        val usernameError: Int? = null,
        val passwordError: Int? = null,
        val isDataValid: Boolean = false
    ) : AuthEvent()

    data class LoginValidation(
        val isLoading: Boolean?,
        val error: Int?
    ) : AuthEvent()

}