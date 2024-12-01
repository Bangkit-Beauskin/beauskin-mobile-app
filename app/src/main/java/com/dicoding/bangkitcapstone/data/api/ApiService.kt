package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.model.AuthResponse
import com.dicoding.bangkitcapstone.data.model.LoginRequest
import com.dicoding.bangkitcapstone.data.model.OtpRequest
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.data.model.RegisterRequest
import com.dicoding.bangkitcapstone.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/auths/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auths/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auths/verify-otp")
    suspend fun verifyOtp(
        @Header("Authorization") token: String,
        @Body request: OtpRequest
    ): Response<OtpResponse>

    @POST("api/v1/auths/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<TokenResponse>
}