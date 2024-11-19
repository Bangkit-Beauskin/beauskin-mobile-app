package com.dicoding.bangkitcapstone.chat

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.R

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}