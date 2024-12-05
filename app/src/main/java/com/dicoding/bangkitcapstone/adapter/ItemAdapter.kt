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
                // Set item name with null safety
                tvName.text = item.name.takeIf { it.isNotBlank() }
                    ?: root.context.getString(R.string.untitled)

                // Handle different item types
                when (item.type) {
                    "product" -> {
                        loadImage(item.url, false)
                        playIcon.isVisible = false
                    }
                    "news" -> {
                        if (!item.url.isNullOrEmpty()) {
                            loadImage(item.url, false)
                        } else {
                            setDefaultImage(R.drawable.baseline_article_24)
                        }
                        playIcon.isVisible = false
                    }
                    "video" -> {
                        if (!item.url.isNullOrEmpty()) {
                            loadImage(item.url, true)
                        } else {
                            setDefaultImage(R.drawable.baseline_movie_24)
                        }
                        playIcon.isVisible = true
                    }
                    else -> {
                        setDefaultImage(R.drawable.baseline_image_24)
                        playIcon.isVisible = false
                    }
                }
            }
        }

        private fun setDefaultImage(resourceId: Int) {
            binding.imageView.setImageResource(resourceId)
        }

        private fun loadImage(url: String?, showPlayIcon: Boolean) {
            binding.apply {
                if (!url.isNullOrEmpty()) {
                    val imageUrl = when {
                        url.contains("youtu.be") -> getYouTubeThumbnailUrl(url)
                        else -> url
                    }

                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(getPlaceholderResource(getItem(bindingAdapterPosition).type))
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
                    setDefaultImage(getPlaceholderResource(getItem(bindingAdapterPosition).type))
                    playIcon.isVisible = showPlayIcon
                }
            }
        }

        private fun getPlaceholderResource(type: String): Int {
            return when (type) {
                "news" -> R.drawable.baseline_article_24
                "video" -> R.drawable.baseline_movie_24
                "product" -> R.drawable.baseline_image_24
                else -> R.drawable.baseline_image_24
            }
        }

        private fun getYouTubeThumbnailUrl(videoUrl: String): String {
            val videoId = try {
                when {
                    videoUrl.contains("youtu.be/") ->
                        videoUrl.substringAfter("youtu.be/").substringBefore("?")
                    videoUrl.contains("youtube.com/watch?v=") ->
                        videoUrl.substringAfter("v=").substringBefore("&")
                    else -> videoUrl
                }
            } catch (e: Exception) {
                videoUrl
            }
            return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
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

    companion object {
        private const val TAG = "ItemAdapter"
    }
}