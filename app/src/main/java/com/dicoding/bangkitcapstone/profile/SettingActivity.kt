package com.dicoding.bangkitcapstone.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.auth.WelcomeActivity
import com.dicoding.bangkitcapstone.databinding.ActivitySettingBinding
import com.dicoding.bangkitcapstone.databinding.DialogLogoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val viewModel: SettingViewModel by viewModels()
    private var logoutDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // Initialize dark mode switch
        binding.switchDarkMode.isChecked = viewModel.isDarkModeEnabled()

        // Setup click listeners
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.logoutCard.setOnClickListener {
            showLogoutDialog()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
            updateDarkMode(isChecked)
        }
    }

    private fun showLogoutDialog() {
        logoutDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val binding = DialogLogoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            window?.apply {
                setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setGravity(Gravity.CENTER)
                setWindowAnimations(R.style.DialogAnimation)
            }

            binding.btnNo.setOnClickListener {
                dismiss()
            }

            binding.btnYes.setOnClickListener {
                viewModel.logout()
                navigateToWelcome()
                dismiss()
            }

            setOnDismissListener {
                logoutDialog = null
            }

            show()
        }
    }

    private fun updateDarkMode(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        logoutDialog?.dismiss()
        logoutDialog = null
    }
}