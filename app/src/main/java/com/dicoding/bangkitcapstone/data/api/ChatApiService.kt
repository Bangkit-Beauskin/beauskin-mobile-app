package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("chat")
    suspend fun sendMessage(@Body message: Map<String, String>): ChatResponse
}