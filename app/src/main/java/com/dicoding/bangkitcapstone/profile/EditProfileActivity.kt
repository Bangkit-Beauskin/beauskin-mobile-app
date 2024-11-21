package com.dicoding.bangkitcapstone.profile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.R

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            // Handle save profile logic here
            finish()
        }

        findViewById<ImageButton>(R.id.btnEditImage).setOnClickListener {
            // Handle image selection logic here
        }
    }
}