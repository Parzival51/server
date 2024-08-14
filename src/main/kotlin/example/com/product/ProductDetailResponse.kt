package example.com.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val product: Product,
    val reviews: List<Review>,
    val averageRating: Double
)