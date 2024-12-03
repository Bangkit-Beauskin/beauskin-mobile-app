package com.dicoding.bangkitcapstone.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.ProfileData
import com.dicoding.bangkitcapstone.data.model.ProfileState
import com.dicoding.bangkitcapstone.data.model.UpdateProfileState
import com.dicoding.bangkitcapstone.databinding.ActivityEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false
    private var isNameChanged = false
    private lateinit var currentPhotoPath: String

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = Uri.fromFile(File(currentPhotoPath))
            isImageChanged = true
            previewSelectedImage(selectedImageUri!!)
            updateSaveButtonState()
        }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isImageChanged = true
            previewSelectedImage(it)
            updateSaveButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
        viewModel.fetchProfile()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener { finish() }
            btnSave.setOnClickListener { handleSave() }
            btnEditImage.setOnClickListener { showImagePickerDialog() }

            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val currentName = s?.toString() ?: ""
                    isNameChanged = currentName != viewModel.getCurrentUsername()
                    updateSaveButtonState()
                }
            })

            btnSave.isEnabled = false
        }
    }

    private fun showImagePickerDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)

        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
            checkCameraPermission()
        }

        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            dialog.dismiss()
            getContent.launch("image/*")
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val photoFile = createImageFile()
        val photoURI = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )
        takePicture.launch(photoURI)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }


    private fun handleSave() {
        val username = binding.edtName.text.toString()
        if (!validateInput(username)) return

        showLoading(true)
        viewModel.updateProfile(username)

        if (isImageChanged && selectedImageUri != null) {
            viewModel.uploadProfilePhoto(selectedImageUri!!, username)
        }
    }

    private fun observeViewModel() {
        viewModel.profileState.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.Success -> {
                    showLoading(false)
                    updateUI(state.data)
                }
                is ProfileState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateProfileState.Loading -> showLoading(true)
                is UpdateProfileState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is UpdateProfileState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun updateUI(profileData: ProfileData) {
        binding.apply {
            val username = profileData.username.ifEmpty {
                getSharedPreferences("auth", Context.MODE_PRIVATE)
                    .getString("email", "")
                    ?.substringBefore("@") ?: ""
            }

            edtName.setText(username)
            loadProfileImage(profileData.profileUrl)

            btnSave.isEnabled = false
            isNameChanged = false
            isImageChanged = false
        }
    }


    private fun loadProfileImage(url: String?) {
        if (url.isNullOrEmpty()) {
            binding.profileImage.setImageResource(R.drawable.baseline_person_24)
            binding.profileImage.setBackgroundResource(R.drawable.profile_placeholder)
            binding.progressBar.visibility = View.GONE
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    binding.profileImage.setImageResource(R.drawable.baseline_person_24)
                    Log.e("EditProfileActivity", "Image load failed for URL: $url", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    binding.profileImage.background = null
                    return false
                }
            })
            .into(binding.profileImage)
    }

    private fun previewSelectedImage(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.profileImage.background = null

        Glide.with(this)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    showError("Failed to load selected image")
                    Log.e("EditProfileActivity", "Preview image load failed", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    Log.d("EditProfileActivity", "Preview image loaded successfully")
                    return false
                }
            })
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

    private fun updateSaveButtonState() {
        val username = binding.edtName.text.toString()
        val isValidName = username.length >= 3
        binding.btnSave.isEnabled = (isNameChanged && isValidName) || isImageChanged
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSave.isEnabled = !isLoading
            btnEditImage.isEnabled = !isLoading
            edtName.isEnabled = !isLoading
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}