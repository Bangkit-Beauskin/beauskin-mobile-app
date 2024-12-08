package com.dicoding.bangkitcapstone.data.model

data class ChatResponse(
    val status: String,
    val response: String,
    val intent: String
)

data class Message(
    val text: String,
    val isUser: Boolean
)