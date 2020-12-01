package com.vaultionizer.vaultapp.ui.auth.data

data class UserDataFormState(val usernameError: Int? = null,
                             val passwordError: Int? = null,
                             val isDataValid: Boolean = false)