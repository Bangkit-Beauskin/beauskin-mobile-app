package com.dicoding.bangkitcapstone.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
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
import com.dicoding.bangkitcapstone.detail.NewsDetailActivity
import com.dicoding.bangkitcapstone.detail.ProductDetailActivity
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanBottomSheetFragment
import com.dicoding.bangkitcapstone.scan.ScanViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ItemAdapter
    private lateinit var scanViewModel: ScanViewModel
    private var isDialogShown = false
    private val tag = "MainActivity"

    // Required permissions
    private val requiredCameraPermission = Manifest.permission.CAMERA
    private val requiredMediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true
            val mediaPermissionGranted = permissions[requiredMediaPermission] == true

            if (cameraPermissionGranted && mediaPermissionGranted) {
                showScanBottomSheet()
                Log.d(tag, "All permissions granted")
            } else {
                showPermissionDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check auth status
        if (!isAuthenticated()) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]

        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
            if (isDialogShown) {
                showPermissionDialog()
            }
        }

        setupDarkMode()
        setupViews()
        setupRecyclerView()
        observeViewModel()
        setupBottomNavigation()
        setupWindowInsets()
        deleteCacheFile()

        // Initial data fetch
        viewModel.fetchItems()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
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
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("item", item)
        }
        startActivity(intent)
    }

    private fun showNewsDetail(item: Item) {
        val intent = Intent(this, NewsDetailActivity::class.java).apply {
            putExtra("item", item)
        }
        startActivity(intent)
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
                    try {
                        checkPermissionForScan()
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

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun checkPermissionForScan() {
        viewModel.checkPermissionForScan(
            requiredCameraPermission = requiredCameraPermission,
            requiredMediaPermission = requiredMediaPermission,
            onPermissionGranted = { showScanBottomSheet() },
            onPermissionDenied = {
                requestPermissionLauncher.launch(
                    arrayOf(requiredCameraPermission, requiredMediaPermission)
                )
            }
        )
    }

    private fun showScanBottomSheet() {
        if (!supportFragmentManager.isDestroyed) {
            try {
                ScanBottomSheetFragment().show(supportFragmentManager, "ScanBottomSheetFragment")
            } catch (e: Exception) {
                Log.e(tag, "Error showing ScanBottomSheet: ${e.message}", e)
            }
        } else {
            Log.e(tag, "FragmentManager is destroyed, cannot show ScanBottomSheet")
        }
    }

    private fun showPermissionDialog() {
        isDialogShown = true
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_rationale))
            .setPositiveButton(getString(R.string.grant)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setOnDismissListener {
                isDialogShown = false
            }
            .create()
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun isFileValid(uri: Uri?): Boolean {
        if (uri == null) return false
        val file = File(cacheDir, uri.lastPathSegment ?: return false)
        return file.exists()
    }

    private fun deleteCacheFile(uri: Uri? = null) {
        Log.d("Cache_EVERYTHING", "Deleting cache file: $uri")
        try {
            val cacheDir = cacheDir
            if (uri != null) {
                val file = File(cacheDir, uri.lastPathSegment ?: return)
                if (file.exists() && file.delete()) {
                    Log.i("Cache_EVERYTHING", "Deleted image file: ${file.absolutePath}")
                } else {
                    Log.w("Cache_EVERYTHING", "File not found or failed to delete: ${file.absolutePath}")
                }
            } else {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_cache_") && file.name.endsWith(".jpg")) {
                        if (file.delete()) {
                            Log.i("Cache_EVERYTHING", "Deleted cache file: ${file.absolutePath}")
                        } else {
                            Log.w("Cache_EVERYTHING", "Failed to delete cache file: ${file.absolutePath}")
                        }
                    } else {
                        Log.i("Cache_EVERYTHING", "Skipped non-image cache file: ${file.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Cache_EVERYTHING", "Error deleting cache file: ${e.localizedMessage}")
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticated()) {
            viewModel.fetchItems()
        } else {
            navigateToLogin()
        }

        // Check and delete cache files for scan images
        scanViewModel.frontImage.value?.let { uri ->
            if (isFileValid(uri)) {
                deleteCacheFile(uri)
                Log.w(tag, "Cache file for front image is invalid, deleted.")
            }
        }

        scanViewModel.leftImage.value?.let { uri ->
            if (isFileValid(uri)) {
                deleteCacheFile(uri)
                Log.w(tag, "Cache file for left image is invalid, deleted.")
            }
        }

        scanViewModel.rightImage.value?.let { uri ->
            if (isFileValid(uri)) {
                deleteCacheFile(uri)
                Log.w(tag, "Cache file for right image is invalid, deleted.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isAuthenticated()) {
            tokenManager.clearTokens()
        }
    }
}