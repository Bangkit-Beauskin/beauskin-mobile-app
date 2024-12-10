package com.dicoding.bangkitcapstone.detail

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

            if (!item.url.isNullOrEmpty()) {
                imageContainer.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE

                Glide.with(this@NewsDetailActivity)
                    .load(item.url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            binding.imageContainer.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }
                    })
                    .error(R.drawable.baseline_broken_image_24)
                    .into(binding.newsImage)
            } else {
                imageContainer.visibility = View.GONE
            }

            newsContent.text = item.description ?: getString(R.string.no_content)

            if (!item.url.isNullOrEmpty()) {
                sourceLabel.visibility = View.VISIBLE
                sourceUrl.visibility = View.VISIBLE
                sourceUrl.text = item.url

                sourceUrl.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                sourceLabel.visibility = View.GONE
                sourceUrl.visibility = View.GONE
            }
        }
    }
}