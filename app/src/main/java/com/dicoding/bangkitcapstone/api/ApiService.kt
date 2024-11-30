package com.dicoding.bangkitcapstone.api

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class ApiService {
    companion object {
        private const val BASE_URL = "https://copycatcapstone.et.r.appspot.com/api/v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    fun register(email: String, password: String, callback: (Result<AuthResponse>) -> Unit) {
        Log.d("ApiService", "Attempting registration with email: $email")

        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url("$BASE_URL/auths/register")
            .post(requestBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequest(request, callback)
    }

    fun login(email: String, password: String, callback: (Result<AuthResponse>) -> Unit) {
        Log.d("ApiService", "Attempting login with email: $email")

        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url("$BASE_URL/auths/login")
            .post(requestBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequest(request, callback)
    }

    fun verifyOtp(otp: String, token: String, callback: (Result<AuthResponse>) -> Unit) {
        Log.d("ApiService", "Original token: $token")

        val formattedToken = when {
            token.startsWith("Bearer ") -> token
            token.startsWith("bearer ") -> "Bearer ${token.substring(7)}"
            else -> "Bearer $token"
        }

        Log.d("ApiService", "Formatted token for verification: $formattedToken")

        val requestBody = JSONObject().apply {
            put("otp", otp)
        }.toString()

        val request = Request.Builder()
            .url("$BASE_URL/auths/verify-otp")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Authorization", formattedToken)
            .addHeader("Content-Type", "application/json")
            .build()

        Log.d("ApiService", "Request URL: ${request.url}")
        Log.d("ApiService", "Request headers: ${request.headers}")
        Log.d("ApiService", "Request body: $requestBody")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Network request failed", e)
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("ApiService", "Response code: ${response.code}")
                Log.d("ApiService", "Response body: $responseBody")

                try {
                    when (response.code) {
                        200 -> {
                            val jsonResponse = JSONObject(responseBody ?: "{}")
                            val authResponse = AuthResponse(
                                code = jsonResponse.optInt("code"),
                                status = jsonResponse.optString("status"),
                                message = jsonResponse.optString("message"),
                                token = jsonResponse.optString("token"),
                                isVerified = jsonResponse.optBoolean("is_verified", false)
                            )
                            callback(Result.success(authResponse))
                        }
                        401 -> {
                            callback(Result.failure(IOException("Session expired")))
                        }
                        else -> {
                            val errorMessage = try {
                                JSONObject(responseBody ?: "").getString("message")
                            } catch (e: Exception) {
                                "Server error: ${response.code}"
                            }
                            callback(Result.failure(IOException(errorMessage)))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ApiService", "Error parsing response", e)
                    callback(Result.failure(e))
                }
            }
        })
    }

    fun refreshToken(oldToken: String, callback: (Result<AuthResponse>) -> Unit) {
        val formattedToken = if (!oldToken.startsWith("Bearer ")) {
            "Bearer $oldToken"
        } else {
            oldToken
        }

        val request = Request.Builder()
            .url("$BASE_URL/auths/refresh-token")
            .post("".toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", formattedToken)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Token refresh failed", e)
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d("ApiService", "Refresh token response: $responseBody")

                    if (response.isSuccessful && responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val authResponse = AuthResponse(
                            code = jsonResponse.optInt("code"),
                            status = jsonResponse.optString("status"),
                            message = jsonResponse.optString("message"),
                            token = jsonResponse.optString("token"),
                            isVerified = jsonResponse.optBoolean("is_verified", false)
                        )
                        callback(Result.success(authResponse))
                    } else {
                        val errorMessage = try {
                            JSONObject(responseBody ?: "").optString("message", "Token refresh failed")
                        } catch (e: Exception) {
                            "Token refresh failed"
                        }
                        callback(Result.failure(IOException(errorMessage)))
                    }
                } catch (e: Exception) {
                    Log.e("ApiService", "Error parsing refresh token response", e)
                    callback(Result.failure(e))
                }
            }
        })
    }

    private fun executeRequest(request: Request, callback: (Result<AuthResponse>) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Request failed", e)
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d("ApiService", "Response code: ${response.code}, body: $responseBody")

                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        val authResponse = parseAuthResponse(json)
                        callback(Result.success(authResponse))
                    } else {
                        val errorMessage = try {
                            JSONObject(responseBody ?: "").optString("message", "Request failed")
                        } catch (e: Exception) {
                            "Request failed"
                        }
                        callback(Result.failure(IOException(errorMessage)))
                    }
                } catch (e: Exception) {
                    Log.e("ApiService", "Error parsing response", e)
                    callback(Result.failure(e))
                }
            }
        })
    }

    private fun parseAuthResponse(json: JSONObject): AuthResponse {
        return AuthResponse(
            code = json.optInt("code"),
            status = json.optString("status"),
            message = json.optString("message"),
            token = json.optString("token"),
            isVerified = json.optBoolean("is_verified", false)
        )
    }
}

