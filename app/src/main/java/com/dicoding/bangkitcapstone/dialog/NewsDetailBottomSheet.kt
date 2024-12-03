package com.dicoding.bangkitcapstone.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.DialogNewsDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewsDetailBottomSheet : BottomSheetDialogFragment() {
    private var _binding: DialogNewsDetailBinding? = null
    private val binding get() = _binding!!

    private val item: Item by lazy {
        requireArguments().getParcelable(ARG_ITEM) ?: throw IllegalArgumentException("Item required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNewsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            tvTitle.text = item.name
            tvContent.text = item.description ?: getString(R.string.no_content)

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

        fun newInstance(item: Item) = NewsDetailBottomSheet().apply {
            arguments = bundleOf(ARG_ITEM to item)
        }
    }
}