package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.Result
import com.dicoding.bangkitcapstone.databinding.FragmentScanSkinType3Binding
import com.dicoding.bangkitcapstone.utils.ImageHandler
import com.dicoding.bangkitcapstone.utils.ImageHandler.deleteCacheFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

// for left image scan
@AndroidEntryPoint
class FragmentScanSkinType3 : Fragment() {

    private val viewModel: ScanViewModel by activityViewModels()

    private var _binding: FragmentScanSkinType3Binding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null

    // Launcher to pick an image from the gallery (Android 13 and above)
    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            handleImageSelection(uri) // handle image
        }

    // Launcher for GetContent (Android below 13)
    private val pickImageLauncherLegacy =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImageSelection(uri) // handle image
        }

    // Launcher for taking a picture from the camera
    private val launcherCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentImageUri?.let { uri ->

                    viewModel.leftImage.value?.let { oldUri ->
                        if (isFileValid(oldUri)) {
                            deleteCacheFile(requireContext(), oldUri)
                            Log.d("Image", "Old image file deleted: $oldUri")
                        } else {
                            Log.w("Image", "Old image file not found, skipping deletion")
                        }
                    }

                    Log.d("Image", "Image file created successfully: URI: $uri")
                    viewModel.setLeftImage(uri)
                    binding.tvErrorMessage.visibility = View.GONE
                    displayImage(uri)
                }
            } else {
                currentImageUri?.let { deleteCacheFile(requireContext(), it) }
                currentImageUri = null
            }
        }

    // Function to create a cache URI for the image
    private fun createCacheUri(originalUri: Uri? = null): Uri? {
        return ImageHandler.createCacheUri(requireContext(), originalUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanSkinType3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the image URI and update UI accordingly
        viewModel.leftImage.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Log.d("Observe for display image", "Displaying image: $uri")
                displayImage(it)
            }
        }

        // Tombol untuk kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanSkinType3", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnUploadApi.setOnClickListener {
            Log.d("UploadApi", "Button clicked")
            if (!areImagesValid()) {
                Log.d("UploadApi", "Images are not valid, handling missing cache.")
                handleMissingCache() // Arahkan ke fragment yang sesuai jika cache hilang
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = getString(R.string.error_no_image_selected)
            } else {
                Log.d("UploadApi", "All images are valid, showing upload confirmation dialog.")
                showUploadConfirmationDialog() // Tampilkan dialog konfirmasi jika semua gambar tersedia
            }
        }

        binding.CaptureCameraBtn3.setOnClickListener {
            Log.d("FragmentScanskintype3", "Start Camera")
            startCamera()
        }

        binding.GrabGallerieBtn3.setOnClickListener {
            Log.d("FragmentScanskintype3", "Open Gallery")
            startGallery()
        }

    }

    // Display the selected image using Glide
    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .clear(binding.ivUpload) // Clear previous image
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.baseline_image_24)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.ivUpload)
    }

    // Function to initiate camera capture and generate a URI for storing the image
    private fun startCamera() {
        currentImageUri = createCacheUri() // Generate URI to store image
        currentImageUri?.let { uri ->
            launcherCamera.launch(uri) // Launch the camera with the generated URI
        } ?: Log.e("Camera", "Failed to create URI for camera image")
    }

    // Open the gallery to pick an image based on Android version
    private fun startGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickImageLauncherLegacy.launch("image/*") // Use GetContent for older versions
        }
    }

    private fun isFileValid(uri: Uri?): Boolean {
        if (uri == null) return false
        val file = File(requireContext().cacheDir, uri.lastPathSegment ?: return false)
        return file.exists() && file.length() > 0
    }

    // Handle selected image
    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                // Delete old cache file if it exists
                viewModel.leftImage.value?.let { oldUri ->
                    if (isFileValid(oldUri)) {
                        deleteCacheFile(
                            requireContext(),
                            oldUri
                        ) // Menghapus file cache lama hanya jika masih ada
                        Log.d("FragmentScanskintype2", "Old image file deleted: $oldUri")
                    } else {
                        Log.w("FragmentScanskintype2", "Old image file not found, skipping deletion")
                    }
                }

                // Create new cache URI for the selected image
                val cacheUri = createCacheUri(uri) ?: uri
                viewModel.setLeftImage(cacheUri)

                binding.tvErrorMessage.visibility = View.GONE
                // Display the selected image
                displayImage(cacheUri)
                Log.d("FragmentScanskintype2", "Image selected and cached: $cacheUri")
            } catch (e: Exception) {
                Log.e(
                    "FragmentScanskintype2",
                    "Failed to handle selected image: ${e.localizedMessage}")
            }
        } else {
            Log.w("FragmentScanskintype2", "No image selected")
        }
    }

    override fun onResume() {
        super.onResume()
        // Periksa jika gambar cache masih ada
        viewModel.leftImage.value?.let { uri ->
            if (!isFileValid(uri)) {

                Log.w("FragmentScanskintype3", "Cached image not found, resetting UI.")
                binding.ivUpload.setImageResource(R.drawable.baseline_image_24)
            }
        }
    }

    private fun areImagesValid(): Boolean {
        val frontImageUri = viewModel.frontImage.value
        val leftImageUri = viewModel.leftImage.value
        val rightImageUri = viewModel.rightImage.value


        Log.d("ImageValidation", "Checking if images are valid...")
        Log.d("ImageValidation", "frontImageUri: $frontImageUri")
        Log.d("ImageValidation", "leftImageUri: $leftImageUri")
        Log.d("ImageValidation", "rightImageUri: $rightImageUri")

        val isValid = isFileValid(frontImageUri) && isFileValid(leftImageUri) && isFileValid(rightImageUri)

        Log.d("ImageValidation", "Are images valid? $isValid")
        return isValid
    }

    private fun showUploadConfirmationDialog() {
        Log.d("UploadDialog", "Showing upload confirmation dialog.")
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_upload_title))
            .setMessage(getString(R.string.confirm_upload_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                Log.d("UploadDialog", "User confirmed upload, starting image upload.")
                viewModel.uploadImages()
                observeUploadimages()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                Log.d("UploadDialog", "User canceled upload, closing dialog.")
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun observeUploadimages() {
        binding.progressBar4.visibility = View.VISIBLE
        viewModel.uploadStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar4.visibility = View.VISIBLE
                    binding.btnUploadApi.isEnabled = false
                    binding.btnBack.isEnabled = false
                    binding.CaptureCameraBtn3.isEnabled = false
                    binding.GrabGallerieBtn3.isEnabled = false

                    binding.btnUploadApi.text = getString(R.string.uploading)
                    binding.tvErrorMessage.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar4.visibility = View.GONE
//                    binding.btnUploadApi.isEnabled = true
//                    binding.btnBack.isEnabled = true
                    binding.btnUploadApi.text = getString(R.string.lets_upload) // Kembalikan teks tombol
                    binding.tvErrorMessage.visibility = View.GONE
                    Toast.makeText(context, result.data, Toast.LENGTH_SHORT).show()

                    Log.d("UploadDialog", "Upload successful: ${result.data}")

                    val bundle = Bundle().apply {
                        putString("responseScan", result.data)

                    }

                    // Menavigasi ke FragmentResultImage dan mengirimkan Bundle
                    findNavController().navigate(R.id.action_fragmentScanSkinType3_to_fragmentResultImage, bundle)
                }
                is Result.Error -> {
                    binding.progressBar4.visibility = View.GONE
                    binding.btnUploadApi.isEnabled = true
                    binding.btnBack.isEnabled = true
                    binding.CaptureCameraBtn3.isEnabled = true
                    binding.GrabGallerieBtn3.isEnabled = true

                    binding.btnUploadApi.text = getString(R.string.lets_upload) // Kembalikan teks tombol
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = result.exception.localizedMessage
                    Log.d("UploadDialog", "Error during upload: ${result.exception.localizedMessage}")
                }
            }
        }
    }


    private fun handleMissingCache() {
            val frontImageUri = viewModel.frontImage.value
            val rightImageUri = viewModel.rightImage.value
            val leftImageUri = viewModel.leftImage.value

            Log.d("MissingCache", "Checking images cache...")
            Log.d("MissingCache", "frontImageUri: $frontImageUri")
            Log.d("MissingCache", "rightImageUri: $rightImageUri")
            Log.d("MissingCache", "leftImageUri: $leftImageUri")

            // Step 1: Check if it's the first load or error is already handled
            if (viewModel.isFirstLoad() && !viewModel.isErrorHandled()) {
                Log.d("MissingCache", "First load detected. Only showing error text.")
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = getString(R.string.error_no_image_selected)
                viewModel.markFirstLoadComplete()
                viewModel.setErrorHandled(true)
                return
            }
            viewModel.setErrorHandled(false)
            // Step 2: Handle the case where images are missing or invalid due to cache deletion
            if (leftImageUri == null || !isFileValid(leftImageUri) ||
                frontImageUri == null || !isFileValid(frontImageUri) ||
                rightImageUri == null || !isFileValid(rightImageUri)) {
                Log.d("MissingCache", "One or more images are missing or invalid due to cache deletion.")
                if (!viewModel.isErrorHandled()) {
                    showErrorDialog()
                    viewModel.setErrorHandled(true)  // Ensure error dialog is only shown once
                }
                viewModel.setErrorHandled(true)
                return
            }
            // Step 3: If images are valid, hide the error message and proceed
            Log.d("MissingCache", "All images are valid. Proceeding with the next steps.")
            binding.tvErrorMessage.visibility = View.GONE  // Hide the error message if everything is valid
        }


    private fun showErrorDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.error_title))  // You can customize this string in your strings.xml
            .setMessage(getString(R.string.error_message))  // Custom error message
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                // After dismissing the dialog, navigate to FragmentInformationScan
                val navController = findNavController()
                navController.navigate(R.id.action_fragmentScanSkinType3_to_fragmentInformationScan)
            }
            .create()

        dialog.show()
    }

}