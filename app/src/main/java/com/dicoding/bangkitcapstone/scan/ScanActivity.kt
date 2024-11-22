package com.dicoding.bangkitcapstone.scan

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import java.io.FileOutputStream

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

                    viewModel.imageUri.value?.let { oldUri -> deleteCacheFile(oldUri) }
                    viewModel.setImageUri(uri)
                    displayImage(uri)
                }
            } else {
                currentImageUri?.let { deleteCacheFile(it) }
                currentImageUri = null

                showToast(getString(R.string.cancel))
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
            viewModel.imageUri.value?.let { uri -> deleteCacheFile(uri) }
            finish()
        }

        binding.btnCaptureAgain.setOnClickListener {
            startCamera()
        }

        binding.btnGrabGallery.setOnClickListener {
            // Launch appropriate gallery picker based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                pickImageLauncherLegacy.launch("image/*") // Launch legacy gallery picker
            }
        }

        binding.btnUploadImageToApi.setOnClickListener {
            val imageUri = viewModel.imageUri.value
            if (imageUri != null) {
                uploadToCloud(imageUri)
            } else {
                showToast("No image available for upload")
            }
        }

    }

    // Start the camera to take a new picture
    private fun startCamera() {
        currentImageUri = createCacheUri() // Create a new URI for the image
        currentImageUri?.let { launcherCamera.launch(it) } // Launch the camera with the URI
    }

    // Create a cache URI
    private fun createCacheUri(originalUri: Uri? = null): Uri? {
        return try {
            val cacheDir = cacheDir
            val file = File(cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")

            // Copy originalUri contents to cache
            originalUri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } catch (e: Exception) {
            Log.e("Cache", "Error creating cache URI: ${e.localizedMessage}")
            null
        }
    }

    // Display the selected image using Glide
    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .clear(binding.previewImageView) // Clear previous image
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_place_holder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.previewImageView)
    }

    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                viewModel.imageUri.value?.let { oldUri ->
                    deleteCacheFile(oldUri)
                    Log.d(tag, "Old image file deleted: $oldUri")
                }
                val cacheUri =
                    createCacheUri(uri) ?: uri
                viewModel.setImageUri(cacheUri)
                displayImage(cacheUri)
                Log.d(tag, "Image selected and cached: $cacheUri")
            } catch (e: Exception) {
                Log.e(tag, "Failed to handle selected image: ${e.localizedMessage}")
                showToast("Failed to process selected image")
            }
        } else {
            showToast(getString(R.string.no_image_selected))
            Log.w(tag, "No image selected")
        }
    }


    // function to upload the image to the cloud
    private fun uploadToCloud(uri: Uri) {
        // example
        Log.d("Upload", "Uploading image to cloud: $uri")

        saveToGallery(uri)

        // After saving to gallery, delete the cached file
        deleteCacheFile(uri)
        Log.i("Cache", "Deleted cache file: $uri")
    }

    // Function to save the image to the gallery with the 'Beauskin' folder
    private fun saveToGallery(uri: Uri) {
        try {
            val resolver = contentResolver
            val cachedUri = createCacheUri(uri) ?: uri

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Beauskin"
                )
            }

            val newImageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (newImageUri != null) {
                resolver.openInputStream(cachedUri)?.use { inputStream ->
                    resolver.openOutputStream(newImageUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                        Log.d("Gallery", "Image successfully saved to gallery: $newImageUri")
                    }
                }

                // After saving to gallery, delete the cached file
                deleteCacheFile(cachedUri)
                Log.i("Cache", "Deleted cached file after saving to gallery: $cachedUri")

            } else {
                Log.e("Gallery", "Failed to create new image entry in MediaStore")
            }
        } catch (e: Exception) {
            Log.e("Gallery", "Error saving image to gallery: ${e.localizedMessage}")
        }
    }

    private fun deleteCacheFile(uri: Uri? = null) {
        try {
            val cacheDir = cacheDir
            if (uri != null) {
                // Delete a specific file
                val file = File(cacheDir, uri.lastPathSegment ?: return)
                if (file.exists() && file.delete()) {
                    Log.i("Cache", "Deleted image file: ${file.absolutePath}")
                } else {
                    Log.w("Cache", "File not found or failed to delete: ${file.absolutePath}")
                }
            } else {
                // Delete all image cache files
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_cache_") && file.name.endsWith(".jpg")) {
                        if (file.delete()) {
                            Log.i("Cache", "Deleted cache file: ${file.absolutePath}")
                        } else {
                            Log.w("Cache", "Failed to delete cache file: ${file.absolutePath}")
                        }
                    } else {
                        Log.i("Cache", "Skipped non-image cache file: ${file.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Cache", "Error deleting cache file: ${e.localizedMessage}")
        }
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.imageUri.value?.let { deleteCacheFile(it) }
    }
}
