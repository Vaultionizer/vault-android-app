package com.vaultionizer.vaultapp.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
// TODO(jatsqi) Remove this later
data class LoggedInUser(
        val userId: String,
        val displayName: String
)