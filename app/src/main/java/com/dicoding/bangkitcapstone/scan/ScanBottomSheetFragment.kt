package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dicoding.bangkitcapstone.databinding.FragmentScanBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ScanBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentScanBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Set up listeners for the camera and gallery buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonCapture.setOnClickListener {
           // startCamera()  //Camera realtime
        }

        binding.imageButtonUpload.setOnClickListener {
            navigateToScanActivity()
            dismiss()
        }
    }

    // Navigate to ScanActivity with the selected image URI
    private fun navigateToScanActivity() {
        val intent = Intent(requireContext(), ScanActivity::class.java)
        Log.d("ScanBottomSheetFragment", "Navigating to ScanActivity")
        startActivity(intent)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}
