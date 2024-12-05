package com.dicoding.bangkitcapstone.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<Item>("item")
            ?: throw IllegalArgumentException("Item required")

        setupViews(item)
    }

    private fun setupViews(item: Item) {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            productName.text = item.name
            skinType.text = getString(
                R.string.skin_type_format,
                item.skinType ?: getString(R.string.all_skin_types)
            )
            description.text = item.description ?: getString(R.string.no_description)

            item.url?.let { url ->
                Glide.with(this@ProductDetailActivity)
                    .load(url)
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.baseline_broken_image_24)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(productImage)
            }
        }
    }
}