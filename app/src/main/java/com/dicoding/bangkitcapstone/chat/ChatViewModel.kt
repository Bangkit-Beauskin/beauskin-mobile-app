package com.dicoding.bangkitcapstone.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.model.Message
import com.dicoding.bangkitcapstone.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    init {
        _messages.value = mutableListOf(Message("Welcome to our chatbot! Feel free to ask me about your skin concerns.", false))
    }


    fun sendMessage(userMessage: String) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(Message(userMessage, true))
        _messages.value = currentMessages

        viewModelScope.launch {
            try {
                val response = repository.sendMessage(userMessage)
                currentMessages.add(Message(response.response, false))
                _messages.value = currentMessages
            } catch (e: Exception) {
                currentMessages.add(Message("Error: ${e.localizedMessage}", false))
                _messages.value = currentMessages
            }
        }
    }
}
