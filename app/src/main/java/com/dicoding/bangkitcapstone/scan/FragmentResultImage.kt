package com.dicoding.bangkitcapstone.scan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.adapter.CarouselAdapter
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import com.dicoding.bangkitcapstone.databinding.FragmentResultImageBinding
import com.dicoding.bangkitcapstone.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs

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

        // Get ScanResponse object from Bundle
        val scanResponse: ScanResponse? = arguments?.getParcelable("responseScan")

        scanResponse?.let {
            setupCarousel(it)
            showHintDialog()
            setupAutoSwipe()
        } ?: run {
            // Handle null case (e.g., show a placeholder or error message)
            Toast.makeText(requireContext(), "No scan data available", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun setupCarousel(scanResponse: ScanResponse) {
        val carouselItems = getCarouselItems(scanResponse)
        val adapter = CarouselAdapter(carouselItems)
        binding.recyclerView.adapter = adapter

        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.attachTo(binding.recyclerView)

        binding.recyclerView.setPageTransformer { page, position ->
            val absPos = abs(position)

            page.scaleY = 1 - (absPos * 0.2f)
            page.alpha = 1 - absPos

            page.scaleX = 1 - (absPos * 0.1f)

            // Menambahkan interpolator kustom jika diperlukan untuk efek lebih halus
            val interpolator = AccelerateDecelerateInterpolator()
            page.translationX = interpolator.getInterpolation(position) * page.width
        }

    }

    private fun showHintDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Yay! Analysis Success")
            .setMessage("Swipe left or right to view the complete analysis!")
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupAutoSwipe() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var currentPage = 0
            override fun run() {
                if (_binding == null)
                    return

                if (currentPage == binding.recyclerView.adapter?.itemCount) currentPage = 0
                binding.recyclerView.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 8000)
            }
        }

        handler.post(runnable)
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
                Log.e(
                    "Fragment",
                    "parentFragmentManager is destroyed, cannot show ResultScanBottomSheetDialog"
                )
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


    data class CarouselItem(
        val imageUrl: String,
        val condition: String,
        val skinType: String,
        val acneTypes: String
    )

    private fun getCarouselItems(scanResponse: ScanResponse): List<CarouselItem> {
        return listOf(
            CarouselItem( // front image
                imageUrl = "${scanResponse.annotatedImages.front}?timestamp=${System.currentTimeMillis()}",
                condition = getString(
                    R.string.front_condition,
                    scanResponse.predictions.front.acneCondition
                ),
                skinType = getString(
                    R.string.front_skin_type,
                    scanResponse.predictions.front.skinType
                ),
                acneTypes = getString(
                    R.string.front_acne_types,
                    if (scanResponse.predictions.front.detectedAcneTypes.isEmpty()) {
                        "-"
                    } else {
                        scanResponse.predictions.front.detectedAcneTypes.joinToString(", ")
                    }
                )
            ),
            CarouselItem( // left image
                imageUrl = "${scanResponse.annotatedImages.left}?timestamp=${System.currentTimeMillis()}",
                condition = getString(
                    R.string.left_condition,
                    scanResponse.predictions.left.acneCondition
                ),
                skinType = getString(
                    R.string.left_skin_type,
                    scanResponse.predictions.left.skinType
                ),
                acneTypes = getString(
                    R.string.left_acne_types,
                    if (scanResponse.predictions.left.detectedAcneTypes.isEmpty()) {
                        "-"
                    } else {
                        scanResponse.predictions.left.detectedAcneTypes.joinToString(", ")
                    }
                )
            ),
            CarouselItem( // right image
                imageUrl = "${scanResponse.annotatedImages.right}?timestamp=${System.currentTimeMillis()}",
                condition = getString(
                    R.string.right_condition,
                    scanResponse.predictions.right.acneCondition
                ),
                skinType = getString(
                    R.string.right_skin_type,
                    scanResponse.predictions.right.skinType
                ),
                acneTypes = getString(
                    R.string.right_acne_types,
                    if (scanResponse.predictions.right.detectedAcneTypes.isEmpty()) {
                        "-"
                    } else {
                        scanResponse.predictions.right.detectedAcneTypes.joinToString(", ")
                    }
                )
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

