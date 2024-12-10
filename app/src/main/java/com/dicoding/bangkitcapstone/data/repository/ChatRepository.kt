package com.dicoding.bangkitcapstone.data.repository

import com.dicoding.bangkitcapstone.data.api.ChatApiService
import com.dicoding.bangkitcapstone.data.model.ChatResponse
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class ChatRepository @Inject constructor
    (@Named("chatbot") private val retrofit: Retrofit) {

    private val chatApiService = retrofit.create(ChatApiService::class.java)

    suspend fun sendMessage(message: String): ChatResponse {
        return chatApiService.sendMessage(mapOf("message" to message))
    }
}