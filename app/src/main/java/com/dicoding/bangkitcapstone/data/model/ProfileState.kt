package com.dicoding.bangkitcapstone.data.model

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val data: ProfileData) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateProfileState {
    data object Loading : UpdateProfileState()
    data class Success(val message: String) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}