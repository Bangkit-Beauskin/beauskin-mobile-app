package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import com.dicoding.bangkitcapstone.databinding.FragmentResultImageBinding
import com.dicoding.bangkitcapstone.main.MainActivity

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
        binding.tvFrontCondition.text = getString(
            R.string.front_condition,
            scanResponse.predictions.front.acneCondition
        )

        binding.tvFrontSkinType.text = getString(
            R.string.front_skin_type,
            scanResponse.predictions.front.skinType
        )

        binding.tvFrontAcneTypes.text = getString(
            R.string.front_acne_types,
            if (scanResponse.predictions.front.detectedAcneTypes.isEmpty()) {
                "-"
            } else {
                scanResponse.predictions.front.detectedAcneTypes.joinToString(", ")
            }
        )

        // Set gambar depan
        loadImage(frontImageUrl, binding.ivFrontAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kiri
        binding.tvLeftCondition.text = getString(
            R.string.left_condition,
            scanResponse.predictions.left.acneCondition
        )
        binding.tvLeftSkinType.text = getString(
            R.string.left_skin_type,
            scanResponse.predictions.left.skinType
        )

        binding.tvLeftAcneTypes.text = getString(
            R.string.left_acne_types,
            if (scanResponse.predictions.left.detectedAcneTypes.isEmpty()) {
                "-"
            } else {
                scanResponse.predictions.left.detectedAcneTypes.joinToString(", ")
            }
        )

        // Set gambar gambar kiri
        loadImage(leftImageUrl, binding.ivLeftAnnotated)

        // Set kondisi dan jenis kulit untuk gambar kanan
        binding.tvRightCondition.text = getString(
            R.string.right_condition,
            scanResponse.predictions.right.acneCondition
        )
        binding.tvRightSkinType.text = getString(
            R.string.right_skin_type,
            scanResponse.predictions.right.skinType
        )
        binding.tvRightAcneTypes.text = getString(
            R.string.right_acne_types,
            if (scanResponse.predictions.right.detectedAcneTypes.isEmpty()) {
                "-"
            } else {
                scanResponse.predictions.right.detectedAcneTypes.joinToString(", ")
            }
        )

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle tombol back pada fragment
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToMainActivity()
        }

        // Handle tombol back dari UI
        binding.btnBack.setOnClickListener {
            navigateToMainActivity()
        }

        binding.btnNextAction.setOnClickListener {
            if (!parentFragmentManager.isDestroyed) {
                try {
                    val bottomSheetDialog = ResultScanBottomSheetDialog()
                    bottomSheetDialog.show(parentFragmentManager, bottomSheetDialog.tag)
                } catch (e: Exception) {
                    Log.e("Fragment", "Error showing ResultScanBottomSheetDialog: ${e.message}", e)
                }
            } else {
                Log.e("Fragment", "parentFragmentManager is destroyed, cannot show ResultScanBottomSheetDialog")
            }
        }

    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val options = android.app.ActivityOptions.makeCustomAnimation(
            requireContext(),
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        Log.d("FragmentResultImage", "Navigating Back to MainActivity")
        startActivity(intent, options.toBundle())
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

