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
                    viewModel.rightImage.value?.let { oldUri -> ImageHandler.deleteCacheFile(requireContext(), oldUri) }
                    Log.d("Image", "Image file created successfully: URI: $uri")
                    viewModel.setRightImage(uri)
                    displayImage(uri)
                }
            } else {
                currentImageUri?.let { ImageHandler.deleteCacheFile(requireContext(), it) }
                currentImageUri = null
                // showToast(getString(R.string.cancel))
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
            Log.d("FragmentScanSkinType2", "Navigating to fragmentScanSkintType3")
            findNavController().navigate(R.id.action_fragmentScanSkinType2_to_fragmentScanSkinType3)
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

    // Handle selected image
    private fun handleImageSelection(uri: Uri?) {
        if (uri != null) {
            try {
                // Delete old cache file if it exists
                viewModel.rightImage.value?.let { oldUri ->
                    ImageHandler.deleteCacheFile(requireContext(), oldUri) // Delete old image cache
                    Log.d("FragmentScanskintype2", "Old image file deleted: $oldUri")
                }

                // Create new cache URI for the selected image
                val cacheUri = createCacheUri(uri) ?: uri
                viewModel.setRightImage(cacheUri)

                // Display the selected image
                displayImage(cacheUri)
                Log.d("FragmentScanskintype2", "Image selected and cached: $cacheUri")
            } catch (e: Exception) {
                Log.e("FragmentScanskintype2", "Failed to handle selected image: ${e.localizedMessage}")
                // showToast("Failed to process selected image")
            }
        } else {
            //   showToast(getString(R.string.no_image_selected))
            Log.w("FragmentScanskintype2", "No image selected")
        }
    }

}