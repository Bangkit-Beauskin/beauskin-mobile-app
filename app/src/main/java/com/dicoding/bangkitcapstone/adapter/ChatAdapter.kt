package com.dicoding.bangkitcapstone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.bangkitcapstone.data.model.Message
import com.dicoding.bangkitcapstone.databinding.ItemChatMessageBinding

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {

            if (message.isUser) {
                binding.userMessageLayout.visibility = View.VISIBLE
                binding.chatbotMessageLayout.visibility = View.GONE
                binding.userMessageText.text = message.text
            } else {
                binding.userMessageLayout.visibility = View.GONE
                binding.chatbotMessageLayout.visibility = View.VISIBLE
                binding.chatbotMessageText.text = message.text
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size
}


