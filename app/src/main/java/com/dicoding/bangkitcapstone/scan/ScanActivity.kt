package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.R

class ScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}