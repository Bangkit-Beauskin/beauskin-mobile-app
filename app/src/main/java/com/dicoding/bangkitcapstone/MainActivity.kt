package com.dicoding.bangkitcapstone

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.databinding.ActivityMainBinding
import com.dicoding.bangkitcapstone.profile.ProfileActivity
import com.dicoding.bangkitcapstone.scan.ScanBottomSheetFragment
import com.dicoding.bangkitcapstone.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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
    private val requiredCameraPermission: String
        get() = Manifest.permission.CAMERA

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupBottomNavigation()

        enableEdgeToEdge()
        setupInsets()

        // Check if the permission dialog was shown previously (handle screen rotation)
        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
            if (isDialogShown) {
                showPermissionDialog()
            }
        }
    }

    // Setup bottom navigation listener and handle navigation to different sections
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_scan -> {
                    checkPermissionForScan() // Check permission for scanning feature
                    false
                }
                R.id.navigation_chat -> {
                    navigateToActivity(ChatActivity::class.java)
                    false
                }
                R.id.navigation_profile -> {
                    navigateToActivity(ProfileActivity::class.java)
                    false
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    // Check if the required permissions are granted, if not, request them
    private fun checkPermissionForScan() {
        mainViewModel.checkPermissionForScan(
            requiredCameraPermission = requiredCameraPermission,
            requiredMediaPermission = requiredMediaPermission,
            onPermissionGranted = { showScanBottomSheet() },
            onPermissionDenied = { requestPermissionLauncher.launch(
                arrayOf(requiredCameraPermission, requiredMediaPermission)
            ) } // Request permissions if denied
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

    // Navigate to the specified activity
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    // Setup insets for the main view to prevent UI overlap with system bars
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
