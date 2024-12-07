package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dicoding.bangkitcapstone.main.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.FragmentInformationScanBinding
import java.io.File

class FragmentInformationScan : Fragment() {
    private var _binding: FragmentInformationScanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            viewModel.frontImage.value?.let { deleteCacheFile(it) }
            viewModel.rightImage.value?.let { deleteCacheFile(it) }
            viewModel.leftImage.value?.let { deleteCacheFile(it) }
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val options = android.app.ActivityOptions.makeCustomAnimation(
                requireContext(),
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            Log.d("FragmentInformationScan", "Navigating Back to MainActivity")
            startActivity(intent, options.toBundle())
            requireActivity().finishAffinity()
        }

        binding.btnNextScan1.setOnClickListener {
            Log.d("FragmentInformationScan", "Navigating to fragmentScanSkintType")
            findNavController().navigate(R.id.action_fragmentInformationScan_to_fragmentScanskintype4)
        }

    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            Log.d("onResume", "frontImage: ${viewModel.frontImage.value}")
            Log.d("onResume", "rightImage: ${viewModel.rightImage.value}")
            Log.d("onResume", "leftImage: ${viewModel.leftImage.value}")

            viewModel.frontImage.value?.let { deleteCacheFile(it) }
            viewModel.rightImage.value?.let { deleteCacheFile(it) }
            viewModel.leftImage.value?.let { deleteCacheFile(it) }

            requireActivity().finish()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}