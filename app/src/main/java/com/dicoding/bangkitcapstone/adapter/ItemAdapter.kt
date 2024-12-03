package com.dicoding.bangkitcapstone.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.ItemGridBinding

class ItemAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: Item) {
            binding.apply {
                tvName.text = item.name

                when (item.type) {
                    "product" -> {
                        loadImage(item.url, false)
                    }
                    "news" -> {
                        imageView.setImageResource(R.drawable.baseline_article_24)
                        playIcon.isVisible = false
                    }
                    "video" -> {
                        loadImage(item.url, true)
                    }
                }
            }
        }

        private fun loadImage(url: String?, showPlayIcon: Boolean) {
            binding.apply {
                if (!url.isNullOrEmpty()) {
                    val imageUrl = when {
                        url.contains("youtu.be") -> getYouTubeThumbnailUrl(url)
                        else -> url
                    }

                    Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(if (showPlayIcon) R.drawable.baseline_movie_24 else R.drawable.baseline_image_24)
                        .error(R.drawable.baseline_broken_image_24)
                        .centerCrop()
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                playIcon.isVisible = showPlayIcon
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                playIcon.isVisible = showPlayIcon
                                return false
                            }
                        })
                        .into(imageView)
                } else {
                    imageView.setImageResource(
                        if (showPlayIcon) R.drawable.baseline_movie_24
                        else R.drawable.baseline_image_24
                    )
                    playIcon.isVisible = showPlayIcon
                }
            }
        }

        private fun getYouTubeThumbnailUrl(videoUrl: String): String {
            val videoId = videoUrl.substringAfterLast("/").substringBefore("?")
            return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}