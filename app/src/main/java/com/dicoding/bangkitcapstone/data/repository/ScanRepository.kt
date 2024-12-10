package com.dicoding.bangkitcapstone.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.dicoding.bangkitcapstone.data.api.ScanApiService
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Named

class ScanRepository @Inject constructor(
    @Named("scan") private val retrofit: Retrofit,
    @ApplicationContext private val context: Context
) {

    private val scanApiService = retrofit.create(ScanApiService::class.java)
    private val mAXFILESIZE = 8 * 1024 * 1024 // 8 MB in bytes

    suspend fun uploadImages(frontImageUri: Uri?, leftImageUri: Uri?, rightImageUri: Uri?): ScanResponse?{
        val frontImagePart = frontImageUri?.let { processImage(it, "front_image") }
        val leftImagePart = leftImageUri?.let { processImage(it, "left_image") }
        val rightImagePart = rightImageUri?.let { processImage(it, "right_image") }

        // Log dan validasi bagian gambar
        Log.d(
            "ScanRepository",
            "frontImagePart: $frontImagePart, leftImagePart: $leftImagePart, rightImagePart: $rightImagePart"
        )

        // Pastikan semua gambar valid sebelum mengunggah
        if (frontImagePart != null && leftImagePart != null && rightImagePart != null) {
            try {
                val response =
                    scanApiService.analyzeSkin(frontImagePart, leftImagePart, rightImagePart)
                Log.d("ScanRepository", "API Response: $response")
                return response  // Return the response from the API

            } catch (e: Exception) {
                Log.e("ScanRepository", "Error during image upload: ${e.localizedMessage}")
                throw e
            }
        } else {
            Log.e("ScanRepository", "One or more images are invalid or missing.")
        }
        return null  // Return null if images are invalid
    }

    // Fungsi untuk memproses gambar
    private fun processImage(uri: Uri, name: String): MultipartBody.Part? {
        val inputStream = getInputStreamFromUri(uri)
        inputStream?.use {
            val fileSize = getFileSizeFromUri(uri)
            Log.d("ScanRepository", "$name original file size: ${fileSize / (1024 * 1024)} MB")

            var bitmap = BitmapFactory.decodeStream(it)
            if (fileSize > mAXFILESIZE) {
                Log.d("ScanRepository", "$name exceeds the 8 MB limit (${fileSize / (1024 * 1024)} MB). Compressing...")
                bitmap = scaleDownBitmap(bitmap, 1920, 1080)
                bitmap = compressImage(bitmap)

                // Correct compressed size calculation
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val compressedSize = byteArrayOutputStream.toByteArray().size.toLong()
                Log.d("ScanRepository", "$name compressed successfully. New size: ${compressedSize / (1024 * 1024)} MB")


                Log.d("ScanRepository", "$name compressed successfully. New size: ${compressedSize / (1024 * 1024)} MB")
            } else {
                Log.d("ScanRepository", "$name is within the size limit. No compression needed.")
            }

            val requestBody = bitmapToRequestBody(bitmap)
            Log.d("ScanRepository", "$name requestBody created for upload")
            return MultipartBody.Part.createFormData(name, "$name.jpg", requestBody)
        }
        return null
    }

    // Fungsi untuk mengompres gambar hingga ukuran sesuai
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 80

        // Kompres hingga ukuran sesuai dengan batas 8 MB
        do {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            quality -= 5
        } while (byteArrayOutputStream.size() > mAXFILESIZE && quality > 10)

        Log.d("ScanRepository", "Image compressed to fit under 8 MB.")
        return BitmapFactory.decodeByteArray(
            byteArrayOutputStream.toByteArray(),
            0,
            byteArrayOutputStream.size()
        )
    }

    // Utility untuk mendapatkan InputStream dari URI
    private fun getInputStreamFromUri(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            Log.e("ScanRepository", "File not found for URI: $uri", e)
            null
        }
    }

    // Utility untuk mendapatkan ukuran file dari URI
    private fun getFileSizeFromUri(uri: Uri): Long {
        var fileSize: Long = 0
        val descriptor = try {
            context.contentResolver.openFileDescriptor(uri, "r")
        } catch (e: Exception) {
            Log.e("ScanRepository", "Error getting file descriptor for URI: $uri", e)
            null
        }

        descriptor?.let {
            try {
                fileSize = it.statSize
            } catch (e: Exception) {
                Log.e("ScanRepository", "Error getting file size for URI: $uri", e)
            } finally {
                try {
                    it.close()
                } catch (e: Exception) {
                    Log.e("ScanRepository", "Error closing file descriptor for URI: $uri", e)
                }
            }
        }
        return fileSize
    }

    // Konversi Bitmap ke RequestBody
    private fun bitmapToRequestBody(bitmap: Bitmap): okhttp3.RequestBody {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray().toRequestBody("image/*".toMediaTypeOrNull())
    }

    private fun scaleDownBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

}
