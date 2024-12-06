package com.dicoding.bangkitcapstone.data.model

import com.google.gson.annotations.SerializedName

data class ScanResponse(
    val status: String,
    val predictions: Predictions,
    @SerializedName("original_images") val originalImages: OriginalImages,
    @SerializedName("annotated_images") val annotatedImages: AnnotatedImages
)

data class Predictions(
    val front: SkinAnalysis,
    val left: SkinAnalysis,
    val right: SkinAnalysis
)

data class SkinAnalysis(
    @SerializedName("acne_condition") val acneCondition: String,
    @SerializedName("skin_type") val skinType: String,
    @SerializedName("detected_acne_types") val detectedAcneTypes: List<String>
)

data class OriginalImages(
    val front: String,
    val left: String,
    val right: String
)

data class AnnotatedImages(
    val front: String,
    val left: String,
    val right: String
)
