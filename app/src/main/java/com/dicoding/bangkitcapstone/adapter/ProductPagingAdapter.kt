package com.dicoding.bangkitcapstone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.databinding.ItemGridBinding

class ProductPagingAdapter(
    private val onItemClick: (Item) -> Unit
) : PagingDataAdapter<Item, ProductPagingAdapter.ItemViewHolder>(ITEM_COMPARATOR) {

    companion object {
        private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    inner class ItemViewHolder(
        private val binding: ItemGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let(onItemClick)
                }
            }
        }

        fun bind(item: Item) {
            binding.apply {
                tvName.text = item.name

                when (item.type) {
                    "news" -> {
                        root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_purple))
                        playIcon.visibility = View.GONE
                    }
                    "video" -> {
                        root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_red))
                        playIcon.visibility = View.VISIBLE
                    }
                    "product" -> {
                        when (item.skin_type?.lowercase()) {
                            "normal" -> {
                                root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_green))
                            }
                            "oily" -> {
                                root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_blue))
                            }
                            "dry" -> {
                                root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_yellow))
                            }
                        }
                        playIcon.visibility = View.GONE
                    }
                }

                // Load image
                if (!item.url.isNullOrEmpty()) {
                    val imageUrl = when {
                        item.url.contains("youtube.com") || item.url.contains("youtu.be") ->
                            getYoutubeThumbnailUrl(item.url)
                        else -> item.url
                    }

                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(when(item.type) {
                            "video" -> R.drawable.baseline_movie_24
                            "news" -> R.drawable.baseline_article_24
                            else -> R.drawable.baseline_image_24
                        })
                        .error(R.drawable.baseline_broken_image_24)
                        .transform(CenterCrop())
                        .into(imageView)
                } else {
                    imageView.setImageResource(when(item.type) {
                        "video" -> R.drawable.baseline_movie_24
                        "news" -> R.drawable.baseline_article_24
                        else -> R.drawable.baseline_image_24
                    })
                }
            }
        }

        private fun getYoutubeThumbnailUrl(videoUrl: String): String {
            val videoId = when {
                videoUrl.contains("youtu.be") -> videoUrl.substringAfterLast("/").substringBefore("?")
                videoUrl.contains("youtube.com") -> videoUrl.substringAfter("v=").substringBefore("&")
                else -> return videoUrl
            }
            return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        }
    }
}