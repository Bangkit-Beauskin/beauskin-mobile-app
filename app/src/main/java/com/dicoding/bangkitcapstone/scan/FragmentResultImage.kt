package com.dicoding.bangkitcapstone.scan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import com.dicoding.bangkitcapstone.databinding.FragmentResultImageBinding

@Suppress("DEPRECATION")
class FragmentResultImage : Fragment() {

    private var _binding: FragmentResultImageBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResultImageBinding.inflate(inflater, container, false)

        // Ambil objek ScanResponse dari Bundle
        val scanResponse: ScanResponse? = arguments?.getParcelable("responseScan")


        if (scanResponse == null) {
            // Handle null case (e.g., show a placeholder or error message)
            Toast.makeText(requireContext(), "No scan data available", Toast.LENGTH_SHORT).show()
        } else {
            displayScanResults(scanResponse)
        }

        return binding.root
    }

    private fun displayScanResults(scanResponse: ScanResponse) {

        Log.d("GlideURL", "Front Image URL: ${scanResponse.annotatedImages.front}")
        Log.d("GlideURL", "Left Image URL: ${scanResponse.annotatedImages.left}")
        Log.d("GlideURL", "Right Image URL: ${scanResponse.annotatedImages.right}")

        val frontImageUrl =
            "${scanResponse.annotatedImages.front}?timestamp=${System.currentTimeMillis()}"
        val leftImageUrl =
            "${scanResponse.annotatedImages.left}?timestamp=${System.currentTimeMillis()}"
        val rightImageUrl =
            "${scanResponse.annotatedImages.right}?timestamp=${System.currentTimeMillis()}"

        // Set kondisi dan jenis kulit untuk gambar depan
        binding.tvFrontCondition.text = scanResponse.predictions.front.acneCondition
        binding.tvFrontSkinType.text = scanResponse.predictions.front.skinType
        binding.tvFrontAcneTypes.text =
            scanResponse.predictions.front.detectedAcneTypes.joinToString(", ")

        // Set gambar depan
        loadImage(frontImageUrl, binding.ivFrontAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kiri
        binding.tvLeftCondition.text = scanResponse.predictions.left.acneCondition
        binding.tvLeftSkinType.text = scanResponse.predictions.left.skinType
        binding.tvLeftAcneTypes.text =
            scanResponse.predictions.left.detectedAcneTypes.joinToString(", ")

        // Set gambar gambar kiri
        loadImage(leftImageUrl, binding.ivLeftAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kanan
        binding.tvRightCondition.text = scanResponse.predictions.right.acneCondition
        binding.tvRightSkinType.text = scanResponse.predictions.right.skinType
        binding.tvRightAcneTypes.text =
            scanResponse.predictions.right.detectedAcneTypes.joinToString(", ")

        // Set gambar gambar kanan
        loadImage(rightImageUrl, binding.ivRightAnnotated)

    }

    private fun loadImage(url: String, imageView: ImageView) {
        Glide.with(requireContext())
            .load(url)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

