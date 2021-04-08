package com.vaultionizer.vaultapp.data.model.rest.user

import com.vaultionizer.vaultapp.data.db.entity.LocalUser

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
// TODO(jatsqi) Remove this later
data class LoggedInUser(
    val localUser: LocalUser,
    val sessionToken: String,
    val webSocketToken: String
)