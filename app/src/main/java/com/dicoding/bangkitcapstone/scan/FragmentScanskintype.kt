package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.FragmentScanskintypeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

// for front image scan
@AndroidEntryPoint
class FragmentScanskintype : Fragment() {

    private val viewModel: ScanViewModel by activityViewModels()

    private var _binding: FragmentScanskintypeBinding? = null
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
                    // Periksa apakah file lama masih valid sebelum menghapusnya
                    viewModel.frontImage.value?.let { oldUri ->
                        if (isFileValid(oldUri)) {
                            deleteCacheFile(oldUri) // Hapus file cache lama hanya jika valid
                            Log.d("Image", "Old image file deleted: $oldUri")
                        } else {
                            Log.w("Image", "Old image file not found, skipping deletion")
                        }
                    }

                    Log.d("Image", "Image file created successfully: URI: $uri")
                    viewModel.setFrontImage(uri)
                    binding.tvErrorMessage.visibility = View.GONE
                    displayImage(uri)
                }
            } else {
                currentImageUri?.let { deleteCacheFile(it) }
                currentImageUri = null
            }
        }

    private fun createCacheUri(originalUri: Uri? = null): Uri? {
        return try {
            val cacheDir = requireContext().cacheDir
            val file = File(cacheDir, "image_cache_${System.currentTimeMillis()}.jpg")

            originalUri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val outputStream = file.outputStream()
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                Log.d(
                    "CreateCacheUri",
                    "File copied from originalUri to cache: ${file.absolutePath}"
                )
            }

            // Buat URI untuk file di cache
            val cacheUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            Log.d(
                "CreateCacheUri",
                "Cache file created successfully: ${file.absolutePath}, URI: $cacheUri"
            )
            cacheUri
        } catch (e: Exception) {
            Log.e("CreateCacheUri", "Failed to create cache file or URI: ${e.localizedMessage}")
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanskintypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the image URI and update UI accordingly
        viewModel.frontImage.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Log.d("Observe for display image", "Displaying image: $uri")
                displayImage(it)
            }
        }


        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanskintype", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnNextImage1.setOnClickListener {

            val frontImageUri = viewModel.frontImage.value

            if (frontImageUri == null || !isFileValid(frontImageUri)) {

                binding.tvErrorMessage.visibility = View.VISIBLE  // Tampilkan pesan error
                binding.tvErrorMessage.text =
                    getString(R.string.error_no_image_selected)  // Pesan error
            } else {
                binding.tvErrorMessage.visibility = View.GONE

                Log.d(
                    "FragmentScanskintype",
                    "Navigating to fragmentScanSkinType2 with Uri: $frontImageUri"
                )
                findNavController().navigate(R.id.action_fragmentScanskintype_to_fragmentScanSkinType22)
            }
        }

        binding.CaptureCameraBtn1.setOnClickListener {
            Log.d("FragmentScanskintype", "Start Camera")
            startCamera()
        }


        binding.GrabGallerieBtn1.setOnClickListener {
            Log.d("FragmentScanskintype", "Open Gallery")
            startGallery()
        }

    }

    // Display the selected image using Glide
    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .clear(binding.ivUploadFront) // Clear previous image
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.baseline_image_24)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.ivUploadFront)
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

    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                // Periksa apakah URI lama masih valid sebelum menghapus cache-nya
                viewModel.frontImage.value?.let { oldUri ->
                    if (isFileValid(oldUri)) {
                        deleteCacheFile(oldUri) // Menghapus file cache lama hanya jika masih ada
                        Log.d("FragmentScanskintype", "Old image file deleted: $oldUri")
                    } else {
                        Log.w("FragmentScanskintype", "Old image file not found, skipping deletion")
                    }
                }

                val cacheUri =
                    createCacheUri(uri) ?: uri
                viewModel.setFrontImage(cacheUri)

                binding.tvErrorMessage.visibility = View.GONE
                //display image
                displayImage(cacheUri)
                Log.d(tag, "Image selected and cached: $cacheUri")
            } catch (e: Exception) {
                Log.e(tag, "Failed to handle selected image: ${e.localizedMessage}")

            }
        } else {

            Log.w(tag, "No image selected")
        }
    }

    private fun deleteCacheFile(uri: Uri? = null) {
        Log.d("Cache", "Deleting cache file: $uri")
        try {
            val cacheDir = requireContext().cacheDir
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

    override fun onResume() {
        super.onResume()

        // Step 1: Check if it's the first load or error is already handled
        if (viewModel.isFirstLoad() && !viewModel.isErrorHandled()) {
            Log.d("MissingCache", "First load detected. Only showing error text.")

            return
        }

        // Step 2: Handle the case where images are missing or invalid due to cache deletion
        viewModel.frontImage.value?.let { uri ->
            if (!isFileValid(uri)) {
                Log.d("MissingCache", "Images are missing or invalid due to cache deletion.")
                if (!viewModel.isErrorHandled()) {
                    showErrorDialog()  // Show the error dialog
                    viewModel.setErrorHandled(true)  // Ensure error dialog is only shown once
                }
                // Reset UI after showing error
                binding.ivUploadFront.setImageResource(R.drawable.baseline_image_24)
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = getString(R.string.error_no_image_selected)
                return
            }
        }

        // Step 3: If images are valid, hide the error message and proceed
        Log.d("MissingCache", "Images are valid. Proceeding with the next steps.")
        binding.tvErrorMessage.visibility = View.GONE  // Hide the error message if everything is valid
    }


    private fun showErrorDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.error_title))
            .setMessage(getString(R.string.error_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()

                val navController = findNavController()
                navController.navigate(R.id.action_fragmentScanskintype_to_fragmentInformationScan
                )
            }
            .create()
        dialog.show()
    }

}