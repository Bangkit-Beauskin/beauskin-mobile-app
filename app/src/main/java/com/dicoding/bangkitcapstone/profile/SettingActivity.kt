package com.dicoding.bangkitcapstone.profile

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.dicoding.bangkitcapstone.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingActivity : AppCompatActivity() {
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
            // Handle logout logic here
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
}