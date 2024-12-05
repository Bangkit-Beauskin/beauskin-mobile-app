package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.databinding.ActivityScanBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
