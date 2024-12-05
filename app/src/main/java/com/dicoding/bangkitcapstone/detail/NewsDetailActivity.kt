package com.dicoding.bangkitcapstone.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<Item>("item")
            ?: throw IllegalArgumentException("Item required")

        setupViews(item)
    }

    private fun setupViews(item: Item) {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            newsTitle.text = item.name
            newsContent.text = item.description ?: getString(R.string.no_content)
        }
    }
}