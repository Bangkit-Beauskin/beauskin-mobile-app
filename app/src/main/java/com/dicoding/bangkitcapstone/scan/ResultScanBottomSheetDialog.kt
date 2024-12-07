package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dicoding.bangkitcapstone.chat.ChatActivity
import com.dicoding.bangkitcapstone.databinding.FragmentResultScanBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ResultScanBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: FragmentResultScanBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultScanBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    // Set up listeners for the camera and gallery buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonScan.setOnClickListener {
            navigateToScanActivity()
            dismiss()
        }

        binding.imageButtonChatbot.setOnClickListener {
            navigateToChatActivity()
            dismiss()
        }
    }

    // Navigate to ScanActivity with the selected image URI
    private fun navigateToScanActivity() {
        val intent = Intent(requireContext(), ScanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val options = android.app.ActivityOptions.makeCustomAnimation(
            requireContext(),
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        Log.d("ResultScanBottomSheetDialog", "Navigating Back to ScanActivity")
        startActivity(intent, options.toBundle())
        requireActivity().finish()
    }


    // Navigate to ScanActivity for re upload
    private fun navigateToChatActivity() {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {}

        val options = android.app.ActivityOptions.makeCustomAnimation(
            requireContext(),
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        Log.d("ResultScanBottomSheetDialog", "Navigating to ChatActivity")
        startActivity(intent, options.toBundle())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}