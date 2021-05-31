package com.vaultionizer.vaultapp.service

import retrofit2.http.PUT

interface SessionService {

    /**
     * Renews the session token for the current user.
     * !! This function is not used at the moment and is missing a proper return type !!
     */
    @PUT("api/session/renew")
    suspend fun renewSession()
}