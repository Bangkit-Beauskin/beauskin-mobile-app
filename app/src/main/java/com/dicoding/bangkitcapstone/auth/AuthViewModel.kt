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
import android.util.Log
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
            Log.d("AuthViewModel", "Starting login for email: $email")
            _authState.value = AuthState.Loading

            try {
                repository.login(email, password).fold(
                    onSuccess = { response ->
                        Log.d("AuthViewModel", "Login response received: ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            response.body()?.let { authResponse ->
                                _authState.value = AuthState.Success(authResponse)
                            } ?: run {
                                _authState.value = AuthState.Error("Empty response body")
                            }
                        } else {
                            _authState.value = AuthState.Error("Login failed: ${response.code()}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "Login error", error)
                        _authState.value = AuthState.Error(error.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during login", e)
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Starting registration for email: $email")
            _authState.value = AuthState.Loading

            try {
                repository.register(email, password).fold(
                    onSuccess = { response ->
                        Log.d("AuthViewModel", "Registration response received: ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            response.body()?.let { authResponse ->
                                _authState.value = AuthState.Success(authResponse)
                            } ?: run {
                                _authState.value = AuthState.Error("Empty response body")
                            }
                        } else {
                            _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "Registration error", error)
                        _authState.value = AuthState.Error(error.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during registration", e)
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Starting OTP verification for code: $otp")
            _otpState.value = OtpState.Loading

            try {
                repository.verifyOtp(otp).fold(
                    onSuccess = { response ->
                        Log.d("AuthViewModel", "OTP verification response received: ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            response.body()?.let { otpResponse ->
                                Log.d("AuthViewModel", "OTP verification message: ${otpResponse.message}")
                                if (otpResponse.message.equals("User verified", ignoreCase = true)) {
                                    _otpState.value = OtpState.Success(OtpResponse(true, otpResponse.message))
                                } else {
                                    _otpState.value = OtpState.Success(otpResponse)
                                }
                            } ?: run {
                                Log.e("AuthViewModel", "Empty response body")
                                _otpState.value = OtpState.Error("Empty response body")
                            }
                        } else {
                            Log.e("AuthViewModel", "OTP verification failed with code: ${response.code()}")
                            _otpState.value = OtpState.Error("Verification failed with code: ${response.code()}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "OTP verification error", error)
                        _otpState.value = OtpState.Error(error.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during OTP verification", e)
                _otpState.value = OtpState.Error("Verification failed: ${e.message}")
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _resendState.value = AuthState.Loading
            repository.resendOtp().fold(
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { authResponse ->
                            _resendState.value = AuthState.Success(authResponse)
                        } ?: run {
                            _resendState.value = AuthState.Error("Empty response body")
                        }
                    } else {
                        _resendState.value = AuthState.Error("Failed to resend OTP: ${response.code()}")
                    }
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