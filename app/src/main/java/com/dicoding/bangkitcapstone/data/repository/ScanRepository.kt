package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dicoding.bangkitcapstone.data.api.ScanApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Named

class ScanRepository @Inject constructor(
    @Named("scan") private val retrofit: Retrofit,
    @ApplicationContext private val context: Context
) {

    private val scanApiService = retrofit.create(ScanApiService::class.java)

    suspend fun uploadImages(frontImageUri: Uri?, leftImageUri: Uri?, rightImageUri: Uri?) {
        val frontImagePart = frontImageUri?.let { uri ->
            val inputStream = getInputStreamFromUri(uri)
            inputStream?.let {
                val requestBody = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "front_image.jpg", requestBody)
            }
        }

        val leftImagePart = leftImageUri?.let { uri ->
            val inputStream = getInputStreamFromUri(uri)
            inputStream?.let {
                val requestBody = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "left_image.jpg", requestBody)
            }
        }

        val rightImagePart = rightImageUri?.let { uri ->
            val inputStream = getInputStreamFromUri(uri)
            inputStream?.let {
                val requestBody = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "right_image.jpg", requestBody)
            }
        }


        // Call API if all images are valid
        if (frontImagePart != null && leftImagePart != null && rightImagePart != null) {
            try {
                scanApiService.analyzeSkin(frontImagePart, leftImagePart, rightImagePart)
            } catch (e: Exception) {
                Log.e("ScanRepository", "Error during image upload: ${e.localizedMessage}")
                throw e
            }
        }
    }

    // Utility function to convert a content URI into an InputStream
    private fun getInputStreamFromUri(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            Log.e("ScanRepository", "File not found for URI: $uri", e)
            null
        }
    }
}