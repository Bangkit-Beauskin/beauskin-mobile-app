package com.dicoding.bangkitcapstone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.scan.FragmentResultImage

class CarouselAdapter(private val items: List<FragmentResultImage.CarouselItem>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivAnnotatedImage)
        private val conditionText: TextView = itemView.findViewById(R.id.tvCondition)
        private val skinTypeText: TextView = itemView.findViewById(R.id.tvSkinType)
        private val acneTypesText: TextView = itemView.findViewById(R.id.tvAcneTypes)

        fun bind(item: FragmentResultImage.CarouselItem) {
            conditionText.text = item.condition
            skinTypeText.text = item.skinType
            acneTypesText.text = item.acneTypes

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)
        }
    }
}