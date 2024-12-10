package com.dicoding.bangkitcapstone.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.auth.WelcomeActivity
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.repository.AuthRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        switchDarkMode.isChecked = sharedPrefs.getBoolean("dark_mode", false)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        findViewById<MaterialCardView>(R.id.logoutCard).setOnClickListener {
            logout()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreate()
        }
    }

    private fun logout() {
        authRepository.clearAuth()
        tokenManager.clearTokens()

        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}