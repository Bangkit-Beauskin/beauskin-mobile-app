package com.dicoding.bangkitcapstone.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ProfileData
)

data class ProfileData(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("profile_url") val profileUrl: String?,
    @SerializedName("is_verified") val isVerified: Boolean
)

data class UpdateProfileRequest(
    @SerializedName("username") val username: String,
    @SerializedName("profile_url") val profileUrl: String? = null
)

data class UpdateProfileResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ProfileUrlData? = null
)

data class ProfileUrlData(
    @SerializedName("profile_url") val profileUrl: String?
)
