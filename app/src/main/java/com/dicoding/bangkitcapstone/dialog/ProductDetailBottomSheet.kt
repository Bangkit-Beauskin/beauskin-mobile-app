package com.dicoding.bangkitcapstone.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.DialogProductDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductDetailBottomSheet : BottomSheetDialogFragment() {
    private var _binding: DialogProductDetailBinding? = null
    private val binding get() = _binding!!

    private val item: Item by lazy {
        requireArguments().getParcelable(ARG_ITEM) ?: throw IllegalArgumentException("Item required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            tvName.text = item.name
            tvDescription.text = item.description ?: getString(R.string.no_description)
            tvSkinType.text = getString(
                R.string.skin_type_format,
                item.skinType ?: getString(R.string.all_skin_types)
            )

            item.url?.let { url ->
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.baseline_broken_image_24)
                    .centerCrop()
                    .into(imageView)
            }

            btnClose.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ITEM = "item"

        fun newInstance(item: Item) = ProductDetailBottomSheet().apply {
            arguments = bundleOf(ARG_ITEM to item)
        }
    }
}