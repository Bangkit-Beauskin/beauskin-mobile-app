package com.dicoding.bangkitcapstone.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.scan.ScanActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    try {
                        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                R.id.navigation_scan -> {
                    try {
                        val intent = Intent(this@ProfileActivity, ScanActivity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
                R.id.navigation_chat -> {
                    try {
                        val intent = Intent(this@ProfileActivity, ChatActivity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.navigation_profile
    }
}