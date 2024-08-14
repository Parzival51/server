package example.com.product

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val products: List<Product>
)