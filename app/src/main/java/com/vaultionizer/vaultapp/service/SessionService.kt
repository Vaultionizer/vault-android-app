package com.vaultionizer.vaultapp.service

import retrofit2.http.Body
import retrofit2.http.PUT
import java.util.*

interface SessionService {
    @PUT("api/session/renew")
    suspend fun renewSession(@Body objects: Objects)
}