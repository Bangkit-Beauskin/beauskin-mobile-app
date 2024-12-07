package com.dicoding.bangkitcapstone.main

import androidx.lifecycle.ViewModel
import com.dicoding.bangkitcapstone.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionUtils: PermissionUtils
) : ViewModel() {

    // Method to check if the required permissions for scanning are granted
    fun checkPermissionForScan(
        requiredMediaPermission: String, // Media permission (e.g., storage or images)
        requiredCameraPermission: String, // Camera permission
        onPermissionGranted: () -> Unit, // Action to take if permissions are granted
        onPermissionDenied: () -> Unit // Action to take if permissions are denied
    ) {
        val cameraPermissionGranted = permissionUtils.hasPermission(requiredCameraPermission)
        val mediaPermissionGranted = permissionUtils.hasPermission(requiredMediaPermission)

        // Check different permission scenarios and take appropriate actions
        when {
            cameraPermissionGranted && mediaPermissionGranted -> {
                onPermissionGranted() // Both permissions granted, proceed with showing scan bottom sheet
            }

            cameraPermissionGranted -> {
                // Camera permission granted, but media permission not granted, request media permission
                onPermissionDenied()
            }

            mediaPermissionGranted -> {
                // Media permission granted, but camera permission not granted, request camera permission
                onPermissionDenied()
            }

            else -> {
                // Neither permission granted, request both permissions
                onPermissionDenied()
            }
        }
    }
}
