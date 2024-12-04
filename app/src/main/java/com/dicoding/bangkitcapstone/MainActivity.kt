package com.dicoding.bangkitcapstone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.bangkitcapstone.auth.LoginActivity
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanBottomSheetFragment
import com.dicoding.bangkitcapstone.ui.main.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var mainViewModel: MainViewModel

    private var isDialogShown = false

    // Logger Tag
    private val tag = "MainActivity"

    // Save the status of the dialog when configuration changes occur (e.g., rotation)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
    }

    // Define the required camera permission for all Android versions
    private val requiredCameraPermission =
        Manifest.permission.CAMERA

    // Define the required media permission based on the Android version (Tiramisu or older)
    private val requiredMediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES // For Android Tiramisu (API 33) and above
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // For older versions
        }

    // Register the permission request result callback
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true
            val mediaPermissionGranted = permissions[requiredMediaPermission] == true

            if (cameraPermissionGranted && mediaPermissionGranted) {
                showScanBottomSheet()
                Log.d(tag, "All permissions granted")
            } else {
                showPermissionDialog() // Show permission dialog if permissions are denied
            }
        }

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

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Check if the permission dialog was shown previously (handle screen rotation)
        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
            if (isDialogShown) {
                showPermissionDialog()
            }
        }

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
                        checkPermissionForScan() // jgn lupa
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

    // Check if the required permissions are granted, if not, request them
    private fun checkPermissionForScan() {
        mainViewModel.checkPermissionForScan(
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

    // Show the scan bottom sheet fragment
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

    // Show a dialog if the permission is denied, asking the user to enable permissions manually
    private fun showPermissionDialog() {
        isDialogShown = true
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_rationale)) // Permission rationale message
            .setPositiveButton(getString(R.string.grant)) { _, _ ->
                // Navigate to app settings to enable permissions
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null) // Cancel button to dismiss dialog
            .setOnDismissListener {
                isDialogShown = false // Reset dialog status when dismissed
            }
            .create()
            .show()
    }


    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}