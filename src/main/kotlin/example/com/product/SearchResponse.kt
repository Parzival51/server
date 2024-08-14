package example.com.product

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val products: List<Product>,
    val categories: List<Category>
)