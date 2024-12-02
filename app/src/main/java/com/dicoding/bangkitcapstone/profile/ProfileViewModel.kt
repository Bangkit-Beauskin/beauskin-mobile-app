package com.dicoding.bangkitcapstone.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.data.model.UpdateProfileState
import com.dicoding.bangkitcapstone.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val context: Context
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
                        currentUsername = response.data.username
                        currentProfileUrl = response.data.profileUrl
                        _profileState.value = ProfileState.Success(response.data)
                    },
                    onFailure = { error ->
                        _profileState.value = ProfileState.Error(error.message ?: "Failed to fetch profile")
                    }
                )
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun uploadProfilePhoto(photoUri: Uri, username: String) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading
            try {
                repository.uploadProfilePhoto(photoUri, username).fold(
                    onSuccess = { response ->
                        currentProfileUrl = response.data?.profileUrl
                        _updateState.value = UpdateProfileState.Success(response.message)
                        fetchProfile()
                    },
                    onFailure = { error ->
                        _updateState.value = UpdateProfileState.Error(error.message ?: "Failed to upload photo")
                    }
                )
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateProfile(username: String) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading
            try {
                repository.updateProfile(username, currentProfileUrl).fold(
                    onSuccess = { response ->
                        currentUsername = username
                        _updateState.value = UpdateProfileState.Success(response.message)
                        fetchProfile()
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
}