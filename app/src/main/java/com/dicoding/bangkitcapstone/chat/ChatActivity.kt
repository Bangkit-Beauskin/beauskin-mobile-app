package com.dicoding.bangkitcapstone.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.bangkitcapstone.adapter.ChatAdapter
import com.dicoding.bangkitcapstone.data.model.Message
import com.dicoding.bangkitcapstone.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    // Flags for scroll state and position
    private var isUserScrolling = false
    private var isAtBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView with adapter and layout manager
        setupRecyclerView()

        // Listen to RecyclerView scroll events
        setupScrollListener()

        // Send message on button click
        setupSendButton()

        // Observe message updates from ViewModel
        observeMessages()

        // Handle EditText focus changes
        setupEditTextFocusListener()

        // Back button behavior
        setupBackButton()

        // Handle soft keyboard visibility and scroll position
        handleKeyboardVisibility()
    }

    // Setup RecyclerView with ChatAdapter
    private fun setupRecyclerView() {
        Log.d("ChatActivity", "Setting up RecyclerView with adapter")
        chatAdapter = ChatAdapter(messageList)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    // Listen to user scrolling behavior
    private fun setupScrollListener() {
        Log.d("ChatActivity", "Setting up scroll listener")
        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isUserScrolling = newState != RecyclerView.SCROLL_STATE_IDLE

                val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                isAtBottom = lastVisiblePosition == chatAdapter.itemCount - 1

                Log.d(
                    "ChatActivity",
                    "Scroll state changed: isUserScrolling = $isUserScrolling, isAtBottom = $isAtBottom"
                )
            }
        })
    }

    // Send message on button click
    private fun setupSendButton() {
        Log.d("ChatActivity", "Setting up send button click listener")
        binding.btnSend.setOnClickListener {
            val userMessage = binding.editTextMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                Log.d("ChatActivity", "Sending message: $userMessage")
                chatViewModel.sendMessage(userMessage)
                binding.editTextMessage.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                binding.editTextMessage.text.clear()
            }
        }
    }

    // Observe message updates and update UI
    private fun observeMessages() {
        chatViewModel.messages.observe(this) { messages ->
            val startSize = messageList.size
            messageList.clear()
            messageList.addAll(messages)

            val isMessagesLargeEnough = messageList.size > 1

            // Notify new message insertion and handle scrolling
            if (messageList.size > startSize) {
                chatAdapter.notifyItemInserted(messageList.size - 1)

                if (isMessagesLargeEnough && (isAtBottom || !isUserScrolling)) {
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
            }

            // After messages are loaded, reset loading UI
            binding.progressBar.visibility = View.GONE
            binding.editTextMessage.isEnabled = true
        }
    }


    // Setup focus change listener on EditText
    private fun setupEditTextFocusListener() {
        binding.editTextMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && isAtBottom && !isUserScrolling) {
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
        }
    }

    // Handle back button click
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            Log.d("ChatActivity", "Back button clicked")
            finish()
        }
    }

    // Handle keyboard visibility and scroll position when keyboard is visible
    private fun handleKeyboardVisibility() {
        binding.chatRecyclerView.viewTreeObserver.addOnPreDrawListener {
            if (isKeyboardVisible() && isAtBottom && !isUserScrolling) {
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
            true
        }
    }

    // Detect if the keyboard is visible
    private fun isKeyboardVisible(): Boolean {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.isAcceptingText
    }
}