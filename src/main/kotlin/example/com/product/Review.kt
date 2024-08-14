package example.com.product

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Review(
    val productId: String,
    val productName: String = "", // Bu alan sunucu tarafından doldurulacak
    val userId: String,
    val username: String,
    val rating: Int,
    val comment: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val date: String = System.currentTimeMillis().toString(),
    val tasteFlavor: Int? = null, // For Food and Beverages
    val tasteFlavorComments: String? = null,
    val pricePerformance: Int? = null, // For multiple categories
    val pricePerformanceComments: String? = null,
    val nutritionalValue: Int? = null, // For Food and Beverages
    val nutritionalValueComments: String? = null,
    val portionSize: Int? = null, // For Food and Beverages
    val portionSizeComments: String? = null,
    val effectiveness: Int? = null, // For Personal Care and Cosmetics, Health and Sports
    val effectivenessComments: String? = null,
    val scent: Int? = null, // For Personal Care and Cosmetics
    val scentComments: String? = null,
    val skinCompatibility: Int? = null, // For Personal Care and Cosmetics
    val skinCompatibilityComments: String? = null,
    val durability: Int? = null, // For multiple categories
    val durabilityComments: String? = null,
    val design: Int? = null, // For multiple categories
    val designComments: String? = null,
    val functionality: Int? = null, // For Home and Living
    val functionalityComments: String? = null,
    val performance: Int? = null, // For Electronics and Technology
    val performanceComments: String? = null,
    val quality: Int? = null, // For Clothing and Fashion
    val qualityComments: String? = null,
    val comfort: Int? = null, // For Clothing and Fashion
    val comfortComments: String? = null,
    val easeOfUse: Int? = null, // For Health and Sports
    val easeOfUseComments: String? = null,
    @BsonId val id: String = ObjectId().toString()
)




