package com.dicoding.bangkitcapstone.data.repository

import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.ProductResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getProducts(): Result<ProductResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getSessionToken() ?: tokenManager.getAccessToken()
            ?: return@withContext Result.failure(Exception("No valid token found"))

            val response = apiService.getProducts("Bearer $token")

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductDetail(id: String): Result<ProductResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductDetail(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch product detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}