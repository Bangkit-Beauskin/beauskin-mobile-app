package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.util.Log
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.model.AuthResponse
import com.dicoding.bangkitcapstone.data.model.LoginRequest
import com.dicoding.bangkitcapstone.data.model.OtpRequest
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.data.model.RegisterRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "auth"
        private const val KEY_TOKEN = "token"
        private const val KEY_VERIFIED = "is_verified"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            Log.d("AuthRepository", "Login response: $response")

            if (response.code == 200) {
                if (response.message == "User fetched") {
                    prefs.edit()
                        .putString(KEY_EMAIL, email)
                        .putString(KEY_PASSWORD, password)
                        .apply()
                }
                Result.success(response)
            } else {
                Log.e("AuthRepository", "Invalid response: ${response.message}")
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            Log.d("AuthRepository", "Register response: $response")

            response.data?.tokenInfo?.access?.let { token: String ->
                prefs.edit()
                    .putString(KEY_TOKEN, token)
                    .putString(KEY_EMAIL, email)
                    .putString(KEY_PASSWORD, password)
                    .putBoolean(KEY_VERIFIED, response.isVerified)
                    .apply()
            }
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register error", e)
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(otp: String): Result<OtpResponse> {
        return try {
            val token = prefs.getString(KEY_TOKEN, null)
            if (token == null) {
                return Result.failure(Exception("No auth token found"))
            }

            val response = apiService.verifyOtp(
                token = "Bearer $token",
                request = OtpRequest(otp = otp)
            )

            if (response.success) {
                prefs.edit()
                    .putBoolean(KEY_VERIFIED, true)
                    .apply()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(): Result<AuthResponse> {
        return try {
            val email = prefs.getString(KEY_EMAIL, null)
            val password = prefs.getString(KEY_PASSWORD, null)

            if (email == null || password == null) {
                return Result.failure(Exception("Login credentials not found"))
            }

            login(email, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}