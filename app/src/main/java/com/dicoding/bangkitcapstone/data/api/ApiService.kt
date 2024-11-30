package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.model.AuthResponse
import com.dicoding.bangkitcapstone.data.model.LoginRequest
import com.dicoding.bangkitcapstone.data.model.OtpRequest
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/auths/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/v1/auths/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/v1/auths/verify-otp")
    suspend fun verifyOtp(
        @Header("Authorization") token: String,
        @Body request: OtpRequest
    ): OtpResponse
}