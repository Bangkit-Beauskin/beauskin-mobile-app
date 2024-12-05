package com.dicoding.bangkitcapstone.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

object ImageHandler {

    fun createCacheUri(context: Context, originalUri: Uri? = null): Uri? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")

            originalUri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("CreateCacheUri", "Copied originalUri to cache: ${file.absolutePath}")
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("CreateCacheUri", "Failed to create cache file: ${e.localizedMessage}")
            null
        }
    }

    fun deleteCacheFile(context: Context, uri: Uri? = null) {
        Log.d("Cache", "Deleting cache file: $uri")
        val cacheDir = context.cacheDir
        try {
            if (uri != null) {
                val file = File(cacheDir, uri.lastPathSegment ?: return)
                if (file.exists() && file.delete()) {
                    Log.d("DeleteCacheFile", "Deleted file: ${file.absolutePath}")
                } else {
                    Log.w("Cache", "File not found or failed to delete: ${file.absolutePath}")
                }

            } else {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_cache_") && file.name.endsWith(".jpg")) {
                        if (file.delete()) {
                            Log.d("DeleteCacheFile", "Deleted cache file: ${file.absolutePath}")
                        }else {
                            Log.w("Cache", "Failed to delete cache file: ${file.absolutePath}")
                        }
                    } else {
                        Log.i("Cache", "Skipped non-image cache file: ${file.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeleteCacheFile", "Failed to delete cache file: ${e.localizedMessage}")
        }
    }
}