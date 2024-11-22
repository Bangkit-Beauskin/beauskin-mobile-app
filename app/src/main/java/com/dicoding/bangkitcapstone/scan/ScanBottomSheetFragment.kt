package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.dicoding.bangkitcapstone.databinding.FragmentScanBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class ScanBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentScanBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val scanViewModel: ScanViewModel by activityViewModels()

    private var currentImageUri: Uri? = null
    private var cameraImageUri: Uri? = null


    // Launcher to pick an image from the gallery (Android 13 and above)
    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                // Generate a cache URI from the picked image
                val cacheUri = createCacheUri(it)
                cacheUri?.let { uri ->
                    currentImageUri = uri
                    scanViewModel.setImageUri(uri) // Update ViewModel with selected URI
                    navigateToScanActivity(uri) // Navigate to ScanActivity
                }
            } ?: Log.d("Photo Picker", "No media selected")
        }

    // Launcher for GetContent (Android below 13)
    private val pickImageLauncherLegacy =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Generate a cache URI from the picked image
                val cacheUri = createCacheUri(it)
                cacheUri?.let { uri ->
                    currentImageUri = uri
                    scanViewModel.setImageUri(uri) // Update ViewModel with selected URI
                    navigateToScanActivity(uri) // Navigate to ScanActivity
                }
            }
        }

    private fun createCacheUri(originalUri: Uri? = null): Uri? {
        return try {
            val cacheDir = requireContext().cacheDir
            val file = File(cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")

            // Jika originalUri tidak null, salin konten dari URI ke cache
            originalUri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val outputStream = file.outputStream()
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                Log.d("CreateCacheUri", "File copied from originalUri to cache: ${file.absolutePath}")
            }

            // Buat URI untuk file di cache
            val cacheUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            Log.d("CreateCacheUri", "Cache file created successfully: ${file.absolutePath}, URI: $cacheUri")
            cacheUri
        } catch (e: Exception) {
            Log.e("CreateCacheUri", "Failed to create cache file or URI: ${e.localizedMessage}")
            null
        }
    }

    // Launcher for taking a picture from the camera
    private val launcherCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let {
                    scanViewModel.setImageUri(it) // Update ViewModel with camera URI
                    navigateToScanActivity(it) // Navigate to ScanActivity
                    dismiss()
                }
            } else {
                Log.d("Camera", "Camera action was not successful")
            }
        }

    // Function to initiate camera capture and generate a URI for storing the image
    private fun startCamera() {
        cameraImageUri = createCacheUri() // Generate URI to store image
        cameraImageUri?.let { uri ->
            launcherCamera.launch(uri) // Launch the camera with the generated URI
        } ?: Log.e("Camera", "Failed to create URI for camera image")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Set up listeners for the camera and gallery buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonCapture.setOnClickListener {
            startCamera()
        }

        binding.imageButtonUpload.setOnClickListener {
            startGallery()
        }
    }

    // Open the gallery to pick an image based on Android version
    private fun startGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickImageLauncherLegacy.launch("image/*") // Use GetContent for older versions
        }
    }

    // Navigate to ScanActivity with the selected image URI
    private fun navigateToScanActivity(uri: Uri) {
        val intent = Intent(requireContext(), ScanActivity::class.java)
        intent.putExtra(EXTRA_IMAGE_URI, uri.toString())
        Log.d("Navigate", "Navigating to ScanActivity with URI: $uri")
        startActivity(intent)
    }

    // Clean up resources and delete the image file when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    companion object {
        const val EXTRA_IMAGE_URI = "imageUri"
    }

}
