package com.aura.data.model

// ApiService.kt
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}
