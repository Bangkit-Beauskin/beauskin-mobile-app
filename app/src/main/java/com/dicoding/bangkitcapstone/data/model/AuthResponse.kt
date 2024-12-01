package com.dicoding.bangkitcapstone.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: TokenData?,
    @SerializedName("status") val status: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean = false
)

data class TokenData(
    @SerializedName("token") val tokenInfo: TokenInfo?
)

data class TokenInfo(
    @SerializedName("access") val access: String?
)

data class TokenResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: TokenData?,
    @SerializedName("status") val status: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class OtpRequest(
    @SerializedName("otp") val otp: String
)

data class OtpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
