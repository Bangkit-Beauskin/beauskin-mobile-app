package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.R

class ScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, FragmentScanskintype())
            .commit()
    }
}