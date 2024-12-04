package com.dicoding.bangkitcapstone.data.repository

import com.dicoding.bangkitcapstone.data.api.ApiService
import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val apiService: ApiService
) {
//    suspend fun uploadImages(front: Uri?, left: Uri?, right: Uri?): String {
//        // Konversi URI ke file atau upload langsung (sesuai kebutuhan API)
//        // Misal menggunakan Retrofit multipart upload
//        return apiService.uploadImages(front, left, right)
//    }
}