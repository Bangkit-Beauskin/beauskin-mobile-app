package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.ActivityScanBinding
import com.dicoding.bangkitcapstone.scan.ScanBottomSheetFragment.Companion.EXTRA_IMAGE_URI
import java.io.File

class ScanActivity : AppCompatActivity() {

    // UI binding and ViewModel
    private lateinit var binding: ActivityScanBinding
    private val viewModel: ScanViewModel by viewModels()

    // Logger Tag and current image URI
    private val tag = "ScanActivity"
    private var currentImageUri: Uri? = null

    // Launcher for Android 13 and above (gallery)
    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            handleImageSelection(uri)
        }

    // Launcher for gallery (below Android 13)
    private val pickImageLauncherLegacy =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImageSelection(uri)
        }

    // Launcher for taking a picture from the camera
    private val launcherCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentImageUri?.let { uri ->
                    // Clean up previous image if any
                    viewModel.imageUri.value?.let { oldUri -> deleteImageFile(oldUri) }
                    // Update ViewModel and display new image
                    viewModel.setImageUri(uri)
                    displayImage(uri)
                }
            } else {
                showToast(getString(R.string.cancel)) // Show toast if the camera action was canceled
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle passed image URI if any
        intent.getStringExtra(EXTRA_IMAGE_URI)?.let { uriString ->
            Uri.parse(uriString).let { uri ->
                viewModel.setImageUri(uri)
            }
        }

        // Set up UI listeners
        setupUiListeners()

        // Observe changes in the image URI and update UI accordingly
        viewModel.imageUri.observe(this) { uri ->
            uri?.let { displayImage(it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current image URI for state restoration
        viewModel.imageUri.value?.let {
            outState.putString(EXTRA_IMAGE_URI, it.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore image URI from saved instance state
        savedInstanceState.getString(EXTRA_IMAGE_URI)?.let { uriString ->
            Uri.parse(uriString).let { uri ->
                viewModel.setImageUri(uri)
            }
        }
    }

    // Set up UI listeners for back, capture again, and gallery pick actions
    private fun setupUiListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.imageUri.value?.let { uri -> deleteImageFile(uri) }
            finish() // Close the activity on back button press
        }

        binding.btnCaptureAgain.setOnClickListener {
            startCamera() // Capture a new image on button press
        }

        binding.btnGrabGallery.setOnClickListener {
            // Launch appropriate gallery picker based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                pickImageLauncherLegacy.launch("image/*") // Launch legacy gallery picker
            }
        }
    }

    // Start the camera to take a new picture
    private fun startCamera() {
        currentImageUri = createImageUri() // Create a new URI for the image
        currentImageUri?.let { launcherCamera.launch(it) } // Launch the camera with the URI
    }

    // Create a URI for storing the taken image
    private fun createImageUri(): Uri? {
        val file = File(filesDir, "camera_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }

    // Display the selected image using Glide
    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .clear(binding.previewImageView) // Clear previous image
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_place_holder)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk cache
            .into(binding.previewImageView)
    }

    // Handle image selection (both from gallery and camera)
    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            viewModel.setImageUri(uri) // Update ViewModel with selected image URI
        } else {
            showToast(getString(R.string.no_image_selected)) // Show toast if no image is selected
        }
    }

    // Delete the image file
    private fun deleteImageFile(uri: Uri) {
        try {
            contentResolver.delete(uri, null, null) // Delete the image file
            Log.i(tag, "Deleted image URI: $uri")
        } catch (e: Exception) {
            Log.e(tag, "Error deleting image file: ${e.localizedMessage}")
        }
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
