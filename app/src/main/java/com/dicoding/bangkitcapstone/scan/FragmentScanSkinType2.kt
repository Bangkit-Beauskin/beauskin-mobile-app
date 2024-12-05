package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.FragmentScanSkinType2Binding
import com.dicoding.bangkitcapstone.utils.ImageHandler
import com.dicoding.bangkitcapstone.utils.ImageHandler.deleteCacheFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

// for right image scan
@AndroidEntryPoint
class FragmentScanSkinType2 : Fragment() {

    private val viewModel: ScanViewModel by activityViewModels()

    private var _binding: FragmentScanSkinType2Binding? = null
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

                    viewModel.rightImage.value?.let { oldUri ->
                        if (isFileValid(oldUri)) {
                            deleteCacheFile(requireContext(), oldUri)
                            Log.d("Image", "Old image file deleted: $oldUri")
                        } else {
                            Log.w("Image", "Old image file not found, skipping deletion")
                        }
                    }

                    Log.d("Image", "Image file created successfully: URI: $uri")
                    viewModel.setRightImage(uri)
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
        _binding = FragmentScanSkinType2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the image URI and update UI accordingly
        viewModel.rightImage.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Log.d("Observe for display image", "Displaying image: $uri")
                displayImage(it)
            }
        }

        // Tombol untuk kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanSkinType2", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnNextImage2.setOnClickListener {

            val rightImageUri = viewModel.rightImage.value

            if (rightImageUri == null || !isFileValid(rightImageUri)) {

                binding.tvErrorMessage.visibility = View.VISIBLE  // Tampilkan pesan error
                binding.tvErrorMessage.text =
                    getString(R.string.error_no_image_selected)  // Pesan error
            } else {
                binding.tvErrorMessage.visibility = View.GONE

                Log.d("FragmentScanSkinType2", "Navigating to fragmentScanSkintType3")
                findNavController().navigate(R.id.action_fragmentScanSkinType2_to_fragmentScanSkinType3)
            }
        }

        binding.CaptureCameraBtn2.setOnClickListener {
            Log.d("FragmentScanskintype2", "Start Camera")
            startCamera()
        }

        binding.GrabGallerieBtn2.setOnClickListener {
            Log.d("FragmentScanskintype2", "Open Gallery")
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


    // Handle selected image
    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                // Delete old cache file if it exists
                viewModel.rightImage.value?.let { oldUri ->
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
                viewModel.setRightImage(cacheUri)

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

//    override fun onResume() {
//        super.onResume()
//
//        // If it's the first load, just set the default image and return
//        if (viewModel.isFirstLoad()) {
//            return
//        }
//
//        // If the right image is not null, check if the file is valid
//        viewModel.rightImage.value?.let { uri ->
//            if (!isFileValid(uri)) {
//                // If the file is invalid (deleted or missing), reset the UI and show error dialog
//                Log.w("FragmentScanskintype", "Cached image not found, resetting UI.")
//                binding.ivUploadFront.setImageResource(R.drawable.baseline_image_24)
//
//                // Mark the first load complete to avoid showing error dialog again
//
//
//                // Set the right image to null in viewModel
//                viewModel.setRightImage(null)
//
//                // Show the error dialog with redirection
//                showErrorDialog()
//            }
//        }
//    }


    override fun onResume() {
        super.onResume()

        // Step 1: Check if it's the first load or error is already handled
        if (viewModel.isFirstLoad() && !viewModel.isErrorHandled()) {
            Log.d("MissingCache", "First load detected. Only showing error text.")
            viewModel.markFirstLoadComplete()
            viewModel.setErrorHandled(false)
            return
        }


        // Step 2: Handle the case where images are missing or invalid due to cache deletion
        viewModel.rightImage.value?.let { uri ->
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
                viewModel.setErrorHandled(true)
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
                navController.navigate(R.id.action_fragmentScanSkinType2_to_fragmentInformationScan)
            }
            .create()

        dialog.show()
    }

}