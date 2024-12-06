package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import com.dicoding.bangkitcapstone.databinding.FragmentResultImageBinding
import com.google.gson.Gson

class FragmentResultImage : Fragment() {

    private lateinit var binding: FragmentResultImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResultImageBinding.inflate(inflater, container, false)

        // Ambil data JSON dari Bundle
        val responseScanJson = arguments?.getString("responseScan")

        // Pastikan data JSON ada
        if (!responseScanJson.isNullOrEmpty()) {
            // Gunakan Gson untuk mengonversi JSON menjadi objek ScanResponse
            val scanResponse = Gson().fromJson(responseScanJson, ScanResponse::class.java)

            // Set data ke UI
            displayScanResults(scanResponse)
        }

        return binding.root
    }

    private fun displayScanResults(scanResponse: ScanResponse) {
        // Set kondisi dan jenis kulit untuk gambar depan
        binding.tvFrontCondition.text = scanResponse.predictions.front.acneCondition
        binding.tvFrontSkinType.text = scanResponse.predictions.front.skinType
        binding.tvFrontAcneTypes.text = scanResponse.predictions.front.detectedAcneTypes.joinToString(", ")

        // Set gambar depan
        Glide.with(requireContext())
            .load(scanResponse.annotatedImages.front)
            .into(binding.ivFrontAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kiri
        binding.tvLeftCondition.text = scanResponse.predictions.left.acneCondition
        binding.tvLeftSkinType.text = scanResponse.predictions.left.skinType
        binding.tvLeftAcneTypes.text = scanResponse.predictions.left.detectedAcneTypes.joinToString(", ")

        // Set gambar kiri
        Glide.with(requireContext())
            .load(scanResponse.annotatedImages.left)
            .into(binding.ivLeftAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kanan
        binding.tvRightCondition.text = scanResponse.predictions.right.acneCondition
        binding.tvRightSkinType.text = scanResponse.predictions.right.skinType
        binding.tvRightAcneTypes.text = scanResponse.predictions.right.detectedAcneTypes.joinToString(", ")

        // Set gambar kanan
        Glide.with(requireContext())
            .load(scanResponse.annotatedImages.right)
            .into(binding.ivRightAnnotated)
    }
}

