package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.bangkitcapstone.databinding.FragmentScanSkinType3Binding


class FragmentScanSkinType3 : Fragment() {


    private var _binding: FragmentScanSkinType3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanSkinType3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol untuk kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanSkinType3", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnUploadApi.setOnClickListener {
            Log.d("FragmentScanSkinType3", "Navigating to fragmentScanSkintType")

        }

    }

}