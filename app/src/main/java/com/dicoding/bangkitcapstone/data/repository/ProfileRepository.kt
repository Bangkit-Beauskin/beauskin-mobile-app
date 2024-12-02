package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.net.Uri
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    suspend fun getProfile(): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getSessionToken() ?: tokenManager.getAccessToken()
            ?: return@withContext Result.failure(Exception("No valid token found"))

            val response = apiService.getProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(photoUri: Uri, username: String): Result<UpdateProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getSessionToken() ?: tokenManager.getAccessToken()
            ?: return@withContext Result.failure(Exception("No valid token found"))

            val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            try {
                context.contentResolver.openInputStream(photoUri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("file", file.name, requestBody)
                val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadProfilePhoto("Bearer $token", photoPart, usernamePart)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to upload photo: ${response.code()}"))
                }
            } finally {
                file.delete()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(username: String, profileUrl: String?): Result<UpdateProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getSessionToken() ?: tokenManager.getAccessToken()
            ?: return@withContext Result.failure(Exception("No valid token found"))

            val request = UpdateProfileRequest(username = username, profileUrl = profileUrl)
            val response = apiService.updateProfile("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}