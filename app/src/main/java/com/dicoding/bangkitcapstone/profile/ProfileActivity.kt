package com.dicoding.bangkitcapstone.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import com.dicoding.bangkitcapstone.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.model.ProfileData
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.databinding.ActivityProfileBinding
import com.dicoding.bangkitcapstone.scan.ScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val EDIT_PROFILE_REQUEST = 100


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
                val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
                startActivityForResult(intent, EDIT_PROFILE_REQUEST)
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
                is ProfileState.Loading -> {
                    showLoading(true)
                }
                is ProfileState.Success -> {
                    showLoading(false)
                    updateUI(state.data)
                }
                is ProfileState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun updateUI(profileData: ProfileData) {
        binding.apply {
            profileName.text = profileData.username
            loadProfileImage(profileData.profileUrl)
            btnEdit.isEnabled = profileData.isVerified
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            fetchProfile()
        }
    }


    private fun loadProfileImage(url: String?) {
        if (url.isNullOrEmpty()) {
            binding.profileImage.setImageResource(R.drawable.baseline_person_24)
            binding.profileImage.setBackgroundResource(R.drawable.profile_placeholder)
            return
        }

        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
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
                    binding.profileImage.setImageResource(R.drawable.baseline_person_24)
                    binding.profileImage.setBackgroundResource(R.drawable.profile_placeholder)
                    Log.e("ProfileActivity", "Image load failed: $url", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.profileImage.background = null
                    return false
                }
            })
            .into(binding.profileImage)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            profileCard.isEnabled = !isLoading
            btnEdit.isEnabled = !isLoading
            btnHistory.isEnabled = !isLoading
            btnSetting.isEnabled = !isLoading
            bottomNavigation.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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