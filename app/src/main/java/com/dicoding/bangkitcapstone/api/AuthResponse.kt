package com.dicoding.bangkitcapstone.api

data class AuthResponse(
    val code: Int,
    val status: String,
    val message: String,
    val token: String?,
    val isVerified: Boolean = false
)

data class ProfileResponse(
    val code: Int,
    val status: String,
    val message: String,
    val data: ProfileData?
)

data class ProfileData(
    val email: String,
    val name: String,
    val photoUrl: String?
)