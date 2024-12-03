package com.dicoding.bangkitcapstone.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.data.model.UpdateProfileState
import com.dicoding.bangkitcapstone.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private var currentUsername: String = ""
    private var currentProfileUrl: String? = null

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _updateState = MutableLiveData<UpdateProfileState>()
    val updateState: LiveData<UpdateProfileState> = _updateState

    fun getCurrentUsername(): String = currentUsername

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.getProfile().fold(
                    onSuccess = { response ->
                        Log.d("ProfileViewModel", "Profile fetch success: ${response.data}")

                        val profileData = response.data.copy(
                            profileUrl = validateProfileUrl(response.data.profileUrl)
                        )

                        _profileState.value = ProfileState.Success(profileData)
                    },
                    onFailure = { error ->
                        Log.e("ProfileViewModel", "Profile fetch error", error)
                        _profileState.value = ProfileState.Error(
                            error.message ?: "Failed to fetch profile"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception in fetchProfile", e)
                _profileState.value = ProfileState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun validateProfileUrl(url: String?): String? {
        return when {
            url == null -> null
            url.isEmpty() -> null
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            url.startsWith("http://") -> url.replace("http://", "https://")
            else -> url
        }.also {
            Log.d("ProfileViewModel", "Validated profile URL: $it")
        }
    }

    fun updateProfile(username: String) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading
            try {
                repository.updateProfile(username, currentProfileUrl).fold(
                    onSuccess = { response ->
                        if (response.code == 200) {
                            currentUsername = username
                            _updateState.value = UpdateProfileState.Success(response.message)
                            fetchProfile()
                        } else {
                            _updateState.value = UpdateProfileState.Error("Failed to update profile: ${response.message}")
                        }
                    },
                    onFailure = { error ->
                        _updateState.value = UpdateProfileState.Error(error.message ?: "Failed to update profile")
                    }
                )
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }


    fun uploadProfilePhoto(photoUri: Uri, username: String) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading
            try {
                repository.uploadProfilePhoto(photoUri, username).fold(
                    onSuccess = { response ->
                        if (response.code == 200) {
                            currentProfileUrl = response.data?.profileUrl
                            _updateState.value = UpdateProfileState.Success(response.message)
                            fetchProfile()
                        } else {
                            _updateState.value = UpdateProfileState.Error("Upload failed: ${response.message}")
                        }
                    },
                    onFailure = { error ->
                        _updateState.value = UpdateProfileState.Error(error.message ?: "Upload failed")
                    }
                )
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error("Upload error: ${e.message}")
            }
        }
    }
}