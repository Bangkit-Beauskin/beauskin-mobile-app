package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.model.ScanResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScanApiService {

    @Multipart
    @POST("analyze")
    suspend fun analyzeSkin(
        @Part frontImage: MultipartBody.Part,
        @Part leftImage: MultipartBody.Part,
        @Part rightImage: MultipartBody.Part
    ): ScanResponse
}