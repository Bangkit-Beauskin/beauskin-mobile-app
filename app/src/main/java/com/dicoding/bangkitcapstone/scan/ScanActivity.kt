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
                    viewModel.imageUri.value?.let { oldUri -> deleteImageFile(oldUri) }
                    viewModel.setImageUri(uri)
                    displayImage(uri)
                }
            } else {
                currentImageUri?.let { deleteImageFile(it) }
                currentImageUri = null
                showToast(getString(R.string.cancel))
            }
        }

    // function to upload the image to the cloud
    private fun uploadToCloud(uri: Uri) {
        // Implement your cloud upload logic here
        Log.d("Upload", "Uploading image to cloud: $uri")

        // After uploading, save the image to the gallery if it came from the camera
        if (uri.toString().contains("cache")) {  // Check if the image is taken from camera
            saveToGallery(uri)
        }

        // After successful upload, delete the image from cache
        deleteImageFile(uri)
    }

    // Function to save the image to the gallery if it's not already there
    private fun saveToGallery(uri: Uri) {
        try {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Beauskin")
            }

            // Check if image already exists in the gallery
            val existingImageUri = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.DATA} = ?",
                arrayOf(uri.path),
                null
            )?.use {
                if (it.moveToFirst()) {
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                } else {
                    null
                }
            }

            if (existingImageUri == null) {
                // Image not found in gallery, proceed with saving
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let { destinationUri ->
                    resolver.openOutputStream(destinationUri)?.use { outputStream ->
                        resolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d("Gallery", "Image saved to gallery: $imageUri")
                }
            } else {
                Log.d("Gallery", "Image already exists in gallery: $existingImageUri")
            }

        } catch (e: Exception) {
            Log.e("Gallery", "Error saving image to gallery: ${e.localizedMessage}")
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

        binding.btnUploadImageToApi.setOnClickListener {
            currentImageUri?.let { uri ->
                uploadToCloud(uri)
            } ?: showToast("No image selected")
        }
    }

    // Start the camera to take a new picture
    private fun startCamera() {
        currentImageUri = createImageUri() // Create a new URI for the image
        currentImageUri?.let { launcherCamera.launch(it) } // Launch the camera with the URI
    }

    // Create a URI for storing the taken image
//    private fun createImageUri(): Uri? {
//        val file = File(filesDir, "camera_image_${System.currentTimeMillis()}.jpg")
//        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
//    }

    private fun createImageUri(): Uri? {
        return try {

            val file = File(cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")

            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

            Log.d("CreateImageUri", "File created successfully: ${file.absolutePath}, URI: $uri")
            uri
        } catch (e: Exception) {

            Log.e("CreateImageUri", "Failed to create file or URI: ${e.localizedMessage}")
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
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk cache
            .into(binding.previewImageView)
    }

    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            // Hapus gambar lama sebelum mengganti dengan yang baru
            viewModel.imageUri.value?.let { oldUri ->
                deleteImageFile(oldUri)
            }
            // Perbarui URI di ViewModel
            viewModel.setImageUri(uri)
        } else {
            showToast(getString(R.string.no_image_selected))
        }
    }

    // Delete the image file
    private fun deleteImageFile(uri: Uri) {
        try {
            val file = File(cacheDir, uri.lastPathSegment ?: return)
            if (file.exists() && file.delete()) {
                Log.i(tag, "Deleted image file: ${file.absolutePath}")
            } else {
                Log.w(tag, "File not found or failed to delete: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error deleting image file: ${e.localizedMessage}")
        }
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.imageUri.value?.let { deleteImageFile(it) }
    }

}
