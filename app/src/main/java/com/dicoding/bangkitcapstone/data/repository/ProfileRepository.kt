package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.ProfileResponse
import com.dicoding.bangkitcapstone.data.model.UpdateProfileRequest
import com.dicoding.bangkitcapstone.data.model.UpdateProfileResponse
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

            Log.d("ProfileRepository", "Fetching profile with token: ${token.take(10)}...")

            val response = apiService.getProfile("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                Log.d("ProfileRepository", "Profile fetch success: ${profileResponse.data}")

                val profileUrl = profileResponse.data.profileUrl
                Log.d("ProfileRepository", "Validated profile URL: $profileUrl")

                Result.success(profileResponse)
            } else {
                Log.e("ProfileRepository", "Profile fetch failed: ${response.code()}")
                Log.e("ProfileRepository", "Error body: ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to get profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Profile fetch exception", e)
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(photoUri: Uri, username: String): Result<UpdateProfileResponse> =
        withContext(Dispatchers.IO) {
            try {

                Log.d("ProfileRepository", "Using existing cached URI for upload: $photoUri")

                val inputStream = context.contentResolver.openInputStream(photoUri)
                    ?: return@withContext Result.failure(Exception("Unable to open input stream for URI: $photoUri"))

                val file = File(context.cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output -> inputStream.copyTo(output) }


                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("file", file.name, requestBody)
                val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())

                val token = tokenManager.getSessionToken() ?: return@withContext Result.failure(
                    Exception("No session token found")
                )

                try {
                    Log.d("ProfileRepository", "Uploading profile photo... $photoPart")
                    val response =
                        apiService.uploadProfilePhoto("Bearer $token", photoPart, usernamePart)

                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        Log.d(
                            "ProfileRepository",
                            "Upload success. New profile URL: ${result.data?.profileUrl}"
                        )
                        Result.success(result)
                    } else {
                        Log.e("ProfileRepository", "Upload failed: ${response.code()}")
                        Result.failure(Exception("Failed to upload photo: ${response.code()}"))
                    }
                } finally {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e("ProfileRepository", "Upload exception", e)
                Result.failure(e)
            }
        }

    suspend fun updateProfile(
        username: String,
        profileUrl: String?
    ): Result<UpdateProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getSessionToken() ?: return@withContext Result.failure(
                Exception("No valid token")
            )

            Log.d("ProfileRepository", "Updating profile - Username: $username, URL: $profileUrl")

            val request = UpdateProfileRequest(username = username, profileUrl = profileUrl)
            val response = apiService.updateProfile("Bearer $token", request)

            Log.d("ProfileRepository", "Update response: $response")

            if (response.isSuccessful && response.body() != null) {
                Log.d("ProfileRepository", "Update successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val error = "Update failed: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e("ProfileRepository", error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Exception during update", e)
            Result.failure(e)
        }
    }
}