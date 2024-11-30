package com.dicoding.bangkitcapstone.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.model.AuthResponse
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _otpState = MutableLiveData<OtpState>()
    val otpState: LiveData<OtpState> = _otpState

    private val _resendState = MutableLiveData<AuthState>()
    val resendState: LiveData<AuthState> = _resendState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password).fold(
                onSuccess = { response ->
                    _authState.value = AuthState.Success(response)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(email, password).fold(
                onSuccess = { response ->
                    _authState.value = AuthState.Success(response)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _otpState.value = OtpState.Loading
            repository.verifyOtp(otp).fold(
                onSuccess = { response ->
                    _otpState.value = OtpState.Success(response)
                },
                onFailure = { error ->
                    _otpState.value = OtpState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _resendState.value = AuthState.Loading
            repository.resendOtp().fold(
                onSuccess = { response ->
                    _resendState.value = AuthState.Success(response)
                },
                onFailure = { error ->
                    _resendState.value = AuthState.Error(error.message ?: "Failed to resend OTP")
                }
            )
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class OtpState {
    object Loading : OtpState()
    data class Success(val response: OtpResponse) : OtpState()
    data class Error(val message: String) : OtpState()
}