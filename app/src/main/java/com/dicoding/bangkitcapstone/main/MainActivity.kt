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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.adapter.FooterLoadStateAdapter
import com.dicoding.bangkitcapstone.adapter.ProductPagingAdapter
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.databinding.ActivityMainBinding
import com.dicoding.bangkitcapstone.detail.NewsDetailActivity
import com.dicoding.bangkitcapstone.detail.ProductDetailActivity
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanActivity
import com.dicoding.bangkitcapstone.scan.ScanViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var pagingAdapter: ProductPagingAdapter
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
                activityScan()
                Log.d(tag, "All permissions granted")
            } else {
                showPermissionDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isAuthenticated()) {
            navigateToLogin()
            return
        }

        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]

        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
            if (isDialogShown) {
                showPermissionDialog()
            }
        }

        setupDarkMode()
        setupRecyclerView()
        setupViews()
        setupBottomNavigation()
        observeData()
        deleteCacheFile()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
    }

    private fun setupDarkMode() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val darkMode = prefs.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupRecyclerView() {
        pagingAdapter = ProductPagingAdapter { item ->
            when (item.type) {
                "product" -> {
                    val intent = Intent(this, ProductDetailActivity::class.java).apply {
                        putExtra("item", item)
                    }
                    startActivity(intent)
                }
                "news" -> {
                    val intent = Intent(this, NewsDetailActivity::class.java).apply {
                        putExtra("item", item)
                    }
                    startActivity(intent)
                }
                "video" -> {
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
            }
        }

        val loadStateAdapter = FooterLoadStateAdapter { pagingAdapter.retry() }

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
            adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)
            setHasFixedSize(true)

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

            // Improve scroll performance
            itemAnimator = null
        }
    }

    private fun setupViews() {
        binding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(insets.left, 0, insets.right, insets.bottom)
                windowInsets
            }

            swipeRefresh.setOnRefreshListener {
                pagingAdapter.refresh()
            }

            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                val checkedChipId = checkedIds.firstOrNull()
                when (checkedChipId) {
                    R.id.chipAll -> viewModel.clearFilters()
                    R.id.chipDry -> viewModel.setSkinType("dry")
                    R.id.chipOily -> viewModel.setSkinType("oily")
                    R.id.chipNormal -> viewModel.setSkinType("normal")
                    R.id.chipNews -> viewModel.setContentType("news")
                    R.id.chipVideo -> viewModel.setContentType("video")
                    null -> viewModel.clearFilters()
                }
            }

            chipGroup.check(R.id.chipAll)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collect { loadStates ->
                    binding.apply {
                        val isLoading = loadStates.refresh is LoadState.Loading
                        progressBar.isVisible = isLoading && pagingAdapter.itemCount == 0
                        swipeRefresh.isRefreshing = isLoading && pagingAdapter.itemCount > 0

                        val errorState = loadStates.source.refresh as? LoadState.Error
                            ?: loadStates.source.append as? LoadState.Error
                            ?: loadStates.source.prepend as? LoadState.Error
                            ?: loadStates.append as? LoadState.Error
                            ?: loadStates.prepend as? LoadState.Error

                        errorState?.let { error ->
                            if (error.error.message?.contains("401") == true || !isAuthenticated()) {
                                tokenManager.clearTokens()
                                navigateToLogin()
                            } else {
                                showError(error.error.message ?: "Unknown error occurred")
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
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
                        Log.e(tag, "Error checking scan permissions: ${e.message}", e)
                    }
                    false
                }
                R.id.navigation_chat -> {
                    try {
                        startActivity(Intent(this, ChatActivity::class.java))
                    } catch (e: Exception) {
                        Log.e(tag, "Error starting chat activity: ${e.message}", e)
                    }
                    false
                }
                R.id.navigation_profile -> {
                    try {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    } catch (e: Exception) {
                        Log.e(tag, "Error starting profile activity: ${e.message}", e)
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
            onPermissionGranted = { activityScan() },
            onPermissionDenied = {
                requestPermissionLauncher.launch(
                    arrayOf(requiredCameraPermission, requiredMediaPermission)
                )
            }
        )
    }

    private fun activityScan() {
        startActivity(Intent(this, ScanActivity::class.java))
    }

    private fun showPermissionDialog() {
        if (isDialogShown) return

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
            .show()
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

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun isFileValid(uri: Uri?): Boolean {
        if (uri == null) return false
        val file = File(cacheDir, uri.lastPathSegment ?: return false)
        return file.exists()
    }

    private fun deleteCacheFile(uri: Uri? = null) {
        Log.d(tag, "Deleting cache file: $uri")
        try {
            val cacheDir = cacheDir
            if (uri != null) {
                val file = File(cacheDir, uri.lastPathSegment ?: return)
                if (file.exists() && file.delete()) {
                    Log.d(tag, "Deleted cache file: ${file.absolutePath}")
                }
            } else {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_cache_") && file.name.endsWith(".jpg")) {
                        if (file.delete()) {
                            Log.d(tag, "Deleted cache file: ${file.absolutePath}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error deleting cache file: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isAuthenticated()) {
            navigateToLogin()
            return
        }

        // Clean up scan image cache files
        scanViewModel.apply {
            frontImage.value?.let { uri ->
                if (isFileValid(uri)) deleteCacheFile(uri)
            }
            leftImage.value?.let { uri ->
                if (isFileValid(uri)) deleteCacheFile(uri)
            }
            rightImage.value?.let { uri ->
                if (isFileValid(uri)) deleteCacheFile(uri)
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