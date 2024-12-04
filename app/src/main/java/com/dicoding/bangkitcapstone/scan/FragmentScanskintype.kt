package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.FragmentScanskintypeBinding


class FragmentScanskintype : Fragment() {

   // private val viewModel: ScanViewModel by activityViewModels() // Shared ViewModel

    private var _binding: FragmentScanskintypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanskintypeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol untuk kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            Log.d("FragmentScanskintype", "Navigating Back to Previous Fragment")
            findNavController().popBackStack()
        }

        binding.btnNextImage1.setOnClickListener {
            Log.d("FragmentScanskintype", "Navigating to fragmentScanSkintType2")
            findNavController().navigate(R.id.action_fragmentScanskintype_to_fragmentScanSkinType22)
        }

    }

}