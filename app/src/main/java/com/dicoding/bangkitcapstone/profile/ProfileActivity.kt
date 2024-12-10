package com.dicoding.bangkitcapstone.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.model.ProfileData
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.databinding.ActivityProfileBinding
import com.dicoding.bangkitcapstone.main.MainActivity
import com.dicoding.bangkitcapstone.scan.ScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var currentProfileData: ProfileData

    private var updatedProfileUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
        fetchProfile()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnEdit.setOnClickListener {
                val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java).apply {
                    putExtra(
                        "CURRENT_USERNAME",
                        currentProfileData.username?.takeIf { it.isNotEmpty() }
                            ?: currentProfileData.email.substringBefore("@"))
                    putExtra("CURRENT_PROFILE_URL", updatedProfileUrl)
                    Log.d(
                        "ProfileActivity",
                        "Starting EditProfileActivity with data: $currentProfileData"
                    )
                }
                startActivity(intent)
            }

            btnSetting.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, SettingActivity::class.java))
            }

            setupBottomNavigation()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                        finish()
                        true
                    }

                    R.id.navigation_scan -> {
                        startActivity(Intent(this@ProfileActivity, ScanActivity::class.java))
                        false
                    }

                    R.id.navigation_chat -> {
                        startActivity(Intent(this@ProfileActivity, ChatActivity::class.java))
                        false
                    }

                    R.id.navigation_profile -> true
                    else -> false
                }
            }
            selectedItemId = R.id.navigation_profile
        }
    }

    private fun observeViewModel() {
        viewModel.profileState.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.Success -> {
                    showLoading(false)
                    currentProfileData = state.data
                    updateUI(currentProfileData)
                }

                is ProfileState.Error -> {
                    showLoading(false)
                    handleError(state.message)
                }
            }
        }
    }

    private fun updateUI(profileData: ProfileData) {
        binding.apply {
            profileName.text = profileData.username?.takeIf { it.isNotEmpty() }
                ?: profileData.email.substringBefore("@")

            btnEdit.isEnabled = profileData.isVerified

            // parameter untuk cache busting
            updatedProfileUrl = "${profileData.profileUrl}?t=${System.currentTimeMillis()}"

            Glide.with(this@ProfileActivity)
                .load(updatedProfileUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .circleCrop()
                .into(profileImage)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            profileCard.isEnabled = !isLoading
            btnEdit.isEnabled = !isLoading
            btnSetting.isEnabled = !isLoading
            bottomNavigation.isEnabled = !isLoading
        }
    }

    private fun handleError(message: String) {
        when {
            message.contains("401") -> logout()
            else -> showError(message)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchProfile() {
        viewModel.fetchProfile()
    }

    private fun logout() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onResume() {
        super.onResume()
        fetchProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}