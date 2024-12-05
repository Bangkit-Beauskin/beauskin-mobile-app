package com.dicoding.bangkitcapstone.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.ui.main.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.model.ProfileData
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.databinding.ActivityProfileBinding
import com.dicoding.bangkitcapstone.scan.ScanActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var imageLoadRetryCount = 0
    private val MAX_RETRY_COUNT = 3

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            fetchProfile()
        }
    }

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
                editProfileLauncher.launch(Intent(this@ProfileActivity, EditProfileActivity::class.java))
            }

            btnHistory.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, HistoryActivity::class.java))
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
                    updateUI(state.data)
                }
                is ProfileState.Error -> {
                    showLoading(false)
                    handleError(state.message)
                }
            }
        }
    }

    private fun updateUI(profileData: ProfileData) {
        Log.d("ProfileActivity", "Updating UI with profile data: $profileData")

        binding.apply {
            profileName.text = profileData.username.ifEmpty {
                profileData.email.substringBefore("@")
            }

            btnEdit.isEnabled = profileData.isVerified

            if (!profileData.isVerified) {
                showVerificationNeededDialog()
            }

            if (profileData.profileUrl.isNullOrEmpty()) {
                Log.d("ProfileActivity", "No profile URL available, using default image")
                profileImage.setImageResource(R.drawable.baseline_person_24)
                profileImage.setBackgroundResource(R.drawable.profile_placeholder)
            } else {
                Log.d("ProfileActivity", "Loading profile image from URL: ${profileData.profileUrl}")
                loadProfileImage(profileData.profileUrl)
            }
        }
    }

    private fun loadProfileImage(url: String?) {
        Log.d("ProfileActivity", "Loading profile image with URL: $url")

        if (!isValidImageUrl(url)) {
            Log.d("ProfileActivity", "Invalid or null URL, using default image")
            binding.profileImage.setImageResource(R.drawable.baseline_person_24)
            binding.profileImage.setBackgroundResource(R.drawable.profile_placeholder)
            binding.progressBar.isVisible = false
            return
        }

        binding.progressBar.isVisible = true

        val secureUrl = url!!.replace("http://", "https://").trim()
        Log.d("ProfileActivity", "Using secure URL: $secureUrl")

        Glide.with(this)
            .load(secureUrl)
            .timeout(30000)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.baseline_person_24)
            .error(R.drawable.baseline_person_24)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    Log.e("ProfileActivity", "Image load failed for URL: $secureUrl", e)
                    e?.logRootCauses("ProfileActivity")

                    if (imageLoadRetryCount < MAX_RETRY_COUNT) {
                        imageLoadRetryCount++
                        Log.d("ProfileActivity", "Retrying image load (attempt $imageLoadRetryCount)")
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadProfileImage(url)
                        }, 1000)
                    } else {
                        Log.e("ProfileActivity", "Max retry attempts reached")
                        showError("Unable to load profile image")
                        imageLoadRetryCount = 0
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    binding.profileImage.background = null
                    imageLoadRetryCount = 0
                    Log.d("ProfileActivity", "Image loaded successfully from $dataSource")
                    return false
                }
            })
            .into(binding.profileImage)
    }

    private fun isValidImageUrl(url: String?): Boolean {
        return !url.isNullOrEmpty() &&
                (url.startsWith("http://") || url.startsWith("https://")) &&
                url != "null"
    }

    private fun showVerificationNeededDialog() {
        Log.d("ProfileActivity", "Verification needed dialog would show here")
    }


    private fun handleError(message: String) {
        when {
            message.contains("401") -> {
                logout()
            }
            else -> showError(message)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            profileCard.isEnabled = !isLoading
            btnEdit.isEnabled = !isLoading
            btnHistory.isEnabled = !isLoading
            btnSetting.isEnabled = !isLoading
            bottomNavigation.isEnabled = !isLoading
        }
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

    private fun fetchProfile() {
        viewModel.fetchProfile()
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