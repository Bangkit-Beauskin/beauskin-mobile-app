package com.dicoding.bangkitcapstone.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Item>
)

@Parcelize
data class Item(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("type") val type: String,
    @SerializedName("skin_type") val skin_type: String?,
    @SerializedName("source") val source: String?
) : Parcelable