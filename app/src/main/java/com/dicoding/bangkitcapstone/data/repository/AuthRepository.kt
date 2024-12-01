package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.util.Log
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    suspend fun login(email: String, password: String): Result<Response<AuthResponse>> {
        return try {
            val response: Response<AuthResponse> = apiService.login(LoginRequest(email, password))
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        body.data?.tokenInfo?.access?.let { token ->
                            tokenManager.saveAccessToken(token)
                            prefs.edit()
                                .putString(KEY_EMAIL, email)
                                .putString(KEY_PASSWORD, password)
                                .apply()
                        }
                        Result.success(response)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                }
                else -> Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Response<AuthResponse>> {
        return try {
            val response: Response<AuthResponse> = apiService.register(RegisterRequest(email, password))
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        body.data?.tokenInfo?.access?.let { token ->
                            tokenManager.saveAccessToken(token)
                            prefs.edit()
                                .putString(KEY_EMAIL, email)
                                .putString(KEY_PASSWORD, password)
                                .apply()
                        }
                        Result.success(response)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                }
                else -> Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register error", e)
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(otp: String): Result<Response<OtpResponse>> {
        return try {
            val accessToken = tokenManager.getAccessToken()
                ?: return Result.failure(Exception("No access token found"))

            val response: Response<OtpResponse> = apiService.verifyOtp(
                token = "Bearer $accessToken",
                request = OtpRequest(otp)
            )

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        if (body.success) {
                            tokenManager.saveSessionToken(accessToken)
                        }
                        Result.success(response)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                }
                else -> Result.failure(Exception("OTP verification failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "OTP verification error", e)
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<Response<TokenResponse>> {
        return try {
            val sessionToken = tokenManager.getSessionToken()
                ?: return Result.failure(Exception("No session token found"))

            val response: Response<TokenResponse> = apiService.refreshToken("Bearer $sessionToken")

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        body.data?.tokenInfo?.access?.let { newToken ->
                            tokenManager.saveSessionToken(newToken)
                        }
                        Result.success(response)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                }
                else -> Result.failure(Exception("Token refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Token refresh error", e)
            Result.failure(e)
        }
    }

    suspend fun resendOtp(): Result<Response<AuthResponse>> {
        return try {
            val email = prefs.getString(KEY_EMAIL, null)
            val password = prefs.getString(KEY_PASSWORD, null)

            if (email == null || password == null) {
                return Result.failure(Exception("Login credentials not found"))
            }

            login(email, password)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Resend OTP error", e)
            Result.failure(e)
        }
    }

    fun clearAuth() {
        tokenManager.clearTokens()
        prefs.edit().clear().apply()
    }
}