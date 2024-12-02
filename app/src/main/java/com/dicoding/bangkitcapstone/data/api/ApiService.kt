package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.model.AuthResponse
import com.dicoding.bangkitcapstone.data.model.LoginRequest
import com.dicoding.bangkitcapstone.data.model.OtpRequest
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.data.model.ProfileResponse
import com.dicoding.bangkitcapstone.data.model.RegisterRequest
import com.dicoding.bangkitcapstone.data.model.TokenResponse
import com.dicoding.bangkitcapstone.data.model.UpdateProfileRequest
import com.dicoding.bangkitcapstone.data.model.UpdateProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

    @Multipart
    @POST("api/v1/profiles")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("username") username: RequestBody
    ): Response<UpdateProfileResponse>

    @POST("api/v1/profiles")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @GET("api/v1/profiles")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
}