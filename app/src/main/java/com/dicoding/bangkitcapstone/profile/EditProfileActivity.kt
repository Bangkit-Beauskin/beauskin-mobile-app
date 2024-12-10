package com.dicoding.bangkitcapstone.profile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.UpdateProfileState
import com.dicoding.bangkitcapstone.databinding.ActivityEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var currentImageUri: Uri? = null
    private var initialUsername: String = ""
    private var initialProfileUrl: String = ""

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data yang dikirim dari ProfileActivity
        initialUsername = intent.getStringExtra("CURRENT_USERNAME") ?: ""
        initialProfileUrl = intent.getStringExtra("CURRENT_PROFILE_URL") ?: ""


        binding.edtName.setText(initialUsername)
        Glide.with(this)
            .load(initialProfileUrl)
            .placeholder(R.drawable.baseline_person_24)
            .error(R.drawable.baseline_person_24)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.profileImage)

        // Tombol kembali
        binding.btnBack.setOnClickListener {

            showDiscardChangesDialog()
            Log.d(
                "EditProfileActivity",
                "Back button clicked. Deleting cache and finishing activity."
            )
        }

        // Add back button handling using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("EditProfileActivity", "Back pressed. Deleting cache and finishing activity.")
                showDiscardChangesDialog()
            }
        })

        // Tombol pilih gambar (Gallery atau Camera)
        binding.btnEditImage.setOnClickListener {
            Log.d("EditProfileActivity", "Profile image clicked, showing image picker.")
            showImagePickerDialog()
        }

        // Periksa apakah ada perubahan pada data
        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                toggleSaveButton()
            }
        })

        // Tombol simpan perubahan
        binding.btnSave.setOnClickListener {
            val updatedUsername = binding.edtName.text.toString()
            val updatedProfileUri = currentImageUri ?: Uri.parse(initialProfileUrl)

            Log.d(
                "EditProfileActivity",
                "Save button clicked. Sending updated data: $updatedUsername, $updatedProfileUri"
            )

            // Validasi input
            if (!validateInput(updatedUsername)) {
                Log.d("EditProfileActivity", "Invalid input. Aborting save process.")
                return@setOnClickListener
            }

            // Update profile in ViewModel
            Log.d("EditProfileActivity", "Updating profile in ViewModel.")
            handleProfileUpdate(updatedUsername, updatedProfileUri)
            observeViewModel()
        }
    }

    private fun handleProfileUpdate(updatedUsername: String, updatedProfileUri: Uri) {
        // Show the loading overlay and progress bar
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        Log.d("EditProfileActivity", "Handling profile update. $updatedUsername, $updatedProfileUri")

        // Check if there's a change in username
        if (updatedUsername != initialUsername) {
            Log.d("EditProfileActivity", "Username changed. Updating profile.")
            viewModel.updateProfile(updatedUsername)
        }

        // Check if there's a change in profile photo
        if (updatedProfileUri != Uri.parse(initialProfileUrl)) {
            Log.d("EditProfileActivity", "Profile photo changed. Updating profile photo. $updatedProfileUri & $updatedUsername")
            viewModel.uploadProfilePhoto(updatedProfileUri, updatedUsername)
        }
    }

    private fun observeViewModel() {
        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateProfileState.Loading -> showLoading(true)
                is UpdateProfileState.Success -> {
                    showLoading(false)
                    finish()
                }

                is UpdateProfileState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Yes") { _, _ ->

                deleteCacheFile()
                finish() }
            .setNegativeButton("No", null)
            .show()
    }

    // Fungsi untuk memilih gambar dari galeri atau kamera
    private fun showImagePickerDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)

        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
            Log.d("EditProfileActivity", "Camera option selected.")
            startCamera()
        }

        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            dialog.dismiss()
            Log.d("EditProfileActivity", "Gallery option selected.")
            startGallery()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Start Camera
    private fun startCamera() {
        Log.d("EditProfileActivity", "Starting camera.")
        deleteCacheFile(currentImageUri)

        currentImageUri = createCacheUri()
        currentImageUri?.let { uri ->
            launcherCamera.launch(uri) // Meluncurkan kamera dengan URI yang dihasilkan
        } ?: Log.e("Camera", "Failed to create URI for camera image")
    }

    // Start Gallery
    private fun startGallery() {
        Log.d("EditProfileActivity", "Starting gallery.")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickImageLauncherLegacy.launch("image/*") // Menggunakan GetContent untuk versi Android lebih lama
        }
    }

    // Pengecekan apakah file gambar valid
    private fun isFileValid(uri: Uri?): Boolean {
        if (uri == null) return false

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->

                return stream.available() > 0
            } ?: false
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error checking file validity: ${e.localizedMessage}")
            false
        }
    }

    // Handle pemilihan gambar
    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                Log.d("EditProfileActivity", "Handling selected image: $uri")
                // Hapus file cache sebelumnya
                deleteCacheFile(currentImageUri)

                val cacheUri =
                    createCacheUri(uri) ?: uri

                // Check if the file is valid before displaying it
                if (isFileValid(cacheUri)) {
                    currentImageUri = cacheUri
                    displayImage(cacheUri)
                    Log.d("Image", "Image selected and cached: $cacheUri")
                } else {
                    Log.e("Image", "Invalid image file")
                }
            } catch (e: Exception) {
                Log.e("Image", "Failed to handle selected image: ${e.localizedMessage}")
            }
        }
    }

    // Tampilkan gambar
    private fun displayImage(uri: Uri) {
        Log.d("EditProfileActivity", "Displaying image: $uri")
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.baseline_image_24)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.profileImage)
    }

    private fun validateInput(username: String): Boolean {
        if (username.isEmpty()) {
            binding.edtName.error = getString(R.string.error_empty_username)
            return false
        }
        if (username.length < 3) {
            binding.edtName.error = getString(R.string.error_username_too_short)
            return false
        }
        return true
    }

    // Menghapus file cache gambar yang lama
    private fun deleteCacheFile(uri: Uri? = null) {
        try {
            val cacheDir = cacheDir
            if (uri != null) {
                val file = File(cacheDir, uri.lastPathSegment ?: return)
                if (file.exists() && file.delete()) {
                    Log.i("Cache", "Deleted image file: ${file.absolutePath}")
                } else {
                    Log.w("Cache", "File not found or failed to delete: ${file.absolutePath}")
                }
            } else {
                // Hapus semua cache file gambar
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_cache_") && file.name.endsWith(".jpg")) {
                        if (file.delete()) {
                            Log.i("Cache", "Deleted cache file: ${file.absolutePath}")
                        } else {
                            Log.w("Cache", "Failed to delete cache file: ${file.absolutePath}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Cache", "Error deleting cache file: ${e.localizedMessage}")
        }
    }

    // Periksa apakah ada perubahan untuk mengaktifkan tombol simpan
    private fun toggleSaveButton() {
        val updatedUsername = binding.edtName.text.toString()
        val updatedProfileUri = currentImageUri ?: Uri.parse(initialProfileUrl)
        Log.d(
            "EditProfileActivity",
            "Checking save button state: $updatedUsername, $updatedProfileUri"
        )

        // Validasi nama pengguna
        if (updatedUsername.isBlank()) {
            binding.btnSave.isEnabled = false
            Log.d("EditProfileActivity", "Save button disabled due to blank username.")
            return
        }

        // Aktifkan tombol jika ada perubahan
        val isSaveEnabled = updatedUsername != initialUsername || updatedProfileUri.toString() != initialProfileUrl
        binding.btnSave.isEnabled = isSaveEnabled
        Log.d("EditProfileActivity", "Save button enabled: $isSaveEnabled")
    }

    // Create URI untuk menyimpan gambar di cache
    private fun createCacheUri(originalUri: Uri? = null): Uri? {
        return try {
            val cacheDir = cacheDir
            val file = File(cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")

            originalUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val outputStream = file.outputStream()
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
            }

            FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("CreateCacheUri", "Failed to create cache file or URI: ${e.localizedMessage}")
            null
        }
    }

    // Launcher untuk mengambil gambar dari kamera
    private val launcherCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentImageUri?.let { uri ->
                    displayImage(uri)
                }
            } else {
                Log.e("EditProfileActivity", "Camera image capture failed.")
            }
        }

    // Launcher untuk memilih gambar dari galeri (Android 13 ke atas)
    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            handleImageSelection(uri)
        }

    // Launcher untuk memilih gambar dari galeri (Android versi lebih lama)
    private val pickImageLauncherLegacy =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImageSelection(uri)
        }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("EditProfileActivity", "Activity destroyed. Cleaning up cache files.")
        deleteCacheFile()
    }

}