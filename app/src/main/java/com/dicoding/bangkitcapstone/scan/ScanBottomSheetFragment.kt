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
                currentImageUri = it
                scanViewModel.setImageUri(it) // Update ViewModel with selected URI
                navigateToScanActivity(it) // Navigate to ScanActivity
            } ?: Log.d("Photo Picker", "No media selected")
        }

    // Launcher for GetContent (Android below 13)
    private val pickImageLauncherLegacy =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                currentImageUri = it
                scanViewModel.setImageUri(it) // Update ViewModel with selected URI
                navigateToScanActivity(it) // Navigate to ScanActivity
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
        cameraImageUri = createImageUri() // Generate URI to store image
        cameraImageUri?.let { uri ->
            launcherCamera.launch(uri) // Launch the camera with the generated URI
        } ?: Log.e("Camera", "Failed to create URI for camera image")
    }

    // Creates a URI to store the image in the app's private storage
//    private fun createImageUri(): Uri? {
//        val file = File(requireContext().filesDir, "camera_image_${System.currentTimeMillis()}.jpg")
//        return FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.fileprovider", // Define the file provider in your manifest
//            file
//        )
//    }

    private fun createImageUri(): Uri? {
        return try {
            val file = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            Log.d("CreateImageUri_ScanBottomSheetFragment", "File created successfully: ${file.absolutePath}, URI: $uri")
            uri
        } catch (e: Exception) {
            Log.e("CreateImageUri_ScanBottomSheetFragment", "Failed to create file or URI: ${e.localizedMessage}")
            null
        }
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
            startCamera() // Start the camera when the capture button is clicked
        }

        binding.imageButtonUpload.setOnClickListener {
            startGallery() // Start the gallery when the upload button is clicked
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
