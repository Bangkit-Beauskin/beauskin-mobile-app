package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.FragmentScanSkinType2Binding


class FragmentScanSkinType2 : Fragment() {

    private var _binding: FragmentScanSkinType2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanSkinType2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol untuk kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanSkinType2", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnNextImage2.setOnClickListener {
            Log.d("FragmentScanSkinType2", "Navigating to fragmentScanSkintType3")
            findNavController().navigate(R.id.action_fragmentScanSkinType2_to_fragmentScanSkinType3)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}