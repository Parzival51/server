package com.example.productrating.product

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageRequest(val url: String)