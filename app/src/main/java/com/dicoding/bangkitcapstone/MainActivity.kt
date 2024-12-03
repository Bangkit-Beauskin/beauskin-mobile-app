package com.dicoding.bangkitcapstone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!tokenManager.isLoggedIn()) {
            Log.d("MainActivity", "User not logged in, redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val darkMode = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        enableEdgeToEdge()
        setupBottomNavigation()
        setupWindowInsets()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    true
                }
                R.id.navigation_scan -> {
                    try {
                        startActivity(Intent(this@MainActivity, ScanActivity::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
                R.id.navigation_chat -> {
                    try {
                        startActivity(Intent(this@MainActivity, ChatActivity::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
                R.id.navigation_profile -> {
                    try {
                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}