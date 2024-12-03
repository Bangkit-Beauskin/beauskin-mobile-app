package com.dicoding.bangkitcapstone.main

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.adapter.ItemAdapter
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.ActivityMainBinding
import com.dicoding.bangkitcapstone.dialog.NewsDetailBottomSheet
import com.dicoding.bangkitcapstone.dialog.ProductDetailBottomSheet
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check auth status
        if (!isAuthenticated()) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDarkMode()
        setupViews()
        setupRecyclerView()
        observeViewModel()
        setupBottomNavigation()

        // Initial data fetch
        viewModel.fetchItems()
    }

    private fun isAuthenticated(): Boolean {
        return tokenManager.isLoggedIn() &&
                (tokenManager.isSessionTokenValid() || tokenManager.isAccessTokenValid())
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setupDarkMode() {
        val darkMode = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupViews() {
        binding.apply {
            swipeRefresh.setOnRefreshListener {
                viewModel.fetchItems()
            }

            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                val skinType = when (checkedIds.firstOrNull()) {
                    R.id.chipDry -> "dry"
                    R.id.chipOily -> "oily"
                    R.id.chipNormal -> "normal"
                    else -> null
                }
                viewModel.setSkinType(skinType)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter { item ->
            when (item.type) {
                "product" -> showProductDetail(item)
                "news" -> showNewsDetail(item)
                "video" -> playVideo(item)
            }
        }

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@MainActivity.adapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(4, 4, 4, 4)
                }
            })
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredItems.collect { items ->
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.apply {
                    progressBar.isVisible = isLoading && adapter.currentList.isEmpty()
                    swipeRefresh.isRefreshing = isLoading && adapter.currentList.isNotEmpty()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let { message ->
                    if (message.contains("401") || !isAuthenticated()) {
                        tokenManager.clearTokens()
                        navigateToLogin()
                    } else {
                        showError(message)
                    }
                }
            }
        }
    }

    private fun showProductDetail(item: Item) {
        val bottomSheet = ProductDetailBottomSheet.newInstance(item)
        bottomSheet.show(supportFragmentManager, "ProductDetail")
    }

    private fun showNewsDetail(item: Item) {
        val bottomSheet = NewsDetailBottomSheet.newInstance(item)
        bottomSheet.show(supportFragmentManager, "NewsDetail")
    }

    private fun playVideo(item: Item) {
        item.url?.let { videoUrl ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                intent.setPackage("com.google.android.youtube")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_scan -> {
                    startActivity(Intent(this@MainActivity, ScanActivity::class.java))
                    false
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this@MainActivity, ChatActivity::class.java))
                    false
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticated()) {
            viewModel.fetchItems()
        } else {
            navigateToLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isAuthenticated()) {
            tokenManager.clearTokens()
        }
    }
}