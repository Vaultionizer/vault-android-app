package com.vaultionizer.vaultapp.service

import retrofit2.http.PUT

interface SessionService {
    @PUT("api/session/renew")
    suspend fun renewSession()
}