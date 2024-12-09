package com.aura.data.model

// ApiService.kt
import com.aura.data.model.transfer.TransferRequest
import com.aura.data.model.transfer.TransferResponse
import com.aura.data.model.account.AccountResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    // New POST /transfer endpoint
    @POST("transfer")
    suspend fun transfer(@Body transferRequest: TransferRequest): TransferResponse

    // New GET /accounts/{id} endpoint
    @GET("accounts/{id}")
    suspend fun getAccount(@Path("id") accountId: String): List<AccountResponse>  // Change to List
}
