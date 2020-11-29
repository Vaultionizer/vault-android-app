package com.vaultionizer.vaultapp.service

import retrofit2.http.*
import java.util.*

interface SessionService {
    @PUT("api/session/renew")
    suspend fun renewSession(@Body objects: Objects)
}