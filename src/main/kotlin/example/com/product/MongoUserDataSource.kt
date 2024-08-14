package example.com.product


import org.litote.kmongo.combine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.io.File


class MongoProductDataSource(
    db: CoroutineDatabase
) : ProductDataSource {

    private val products = db.getCollection<Product>()
    private val reviews = db.getCollection<Review>()

    override suspend fun addProduct(product: Product): Boolean {
        return products.insertOne(product).wasAcknowledged()
    }

    override suspend fun getAllProducts(): List<Product> {
        val productList = products.find().toList()
        // Her ürün için ortalama yıldızları güncelle
        productList.forEach { product ->
            updateProductAverageRating(product.id)
        }
        return productList
    }

    override suspend fun getProductById(id: String): Product? {
        val product = products.findOne(Product::id eq id)
        // Ürünün ortalama yıldızını güncelle
        if (product != null) {
            updateProductAverageRating(product.id)
        }
        return product
    }

    override suspend fun uploadImage(image: ByteArray, fileName: String): String {
        val file = File("uploads/$fileName")
        file.writeBytes(image)
        return "http://localhost:8080/uploads/$fileName"
    }

    override suspend fun addReview(review: Review): Boolean {
        val wasAcknowledged = reviews.insertOne(review).wasAcknowledged()
        if (wasAcknowledged) {
            updateProductAverageRating(review.productId) // productId kullanarak güncelleyin
        }
        return wasAcknowledged
    }

    override suspend fun getReviewsByProductId(productId: String): List<Review> {
        return reviews.find(Review::productId eq productId).toList()
    }

    override suspend fun getReviewById(reviewId: String): Review? {
        return reviews.findOne(Review::id eq reviewId)
    }

    override suspend fun updateReviewLikes(reviewId: String, likes: Int, likedBy: List<String>): Boolean {
        val update = combine(setValue(Review::likes, likes), setValue(Review::likedBy, likedBy))
        return reviews.updateOne(Review::id eq reviewId, update).wasAcknowledged()
    }

    override suspend fun updateReviewDislikes(reviewId: String, dislikes: Int, dislikedBy: List<String>): Boolean {
        val update = combine(setValue(Review::dislikes, dislikes), setValue(Review::dislikedBy, dislikedBy))
        return reviews.updateOne(Review::id eq reviewId, update).wasAcknowledged()
    }

    private suspend fun updateProductAverageRating(productId: String) {
        val reviews = getReviewsByProductId(productId)

        // Mevcut yıldız ortalamasını hesaplama (eski fonksiyonel yapı korunur)
        val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

        // Kriterlere göre ortalamaları hesaplama
        val averageTasteFlavor = if (reviews.any { it.tasteFlavor != null }) reviews.mapNotNull { it.tasteFlavor }.average() else null
        val averagePricePerformance = if (reviews.any { it.pricePerformance != null }) reviews.mapNotNull { it.pricePerformance }.average() else null
        val averageNutritionalValue = if (reviews.any { it.nutritionalValue != null }) reviews.mapNotNull { it.nutritionalValue }.average() else null
        val averagePortionSize = if (reviews.any { it.portionSize != null }) reviews.mapNotNull { it.portionSize }.average() else null
        val averageEffectiveness = if (reviews.any { it.effectiveness != null }) reviews.mapNotNull { it.effectiveness }.average() else null
        val averageScent = if (reviews.any { it.scent != null }) reviews.mapNotNull { it.scent }.average() else null
        val averageSkinCompatibility = if (reviews.any { it.skinCompatibility != null }) reviews.mapNotNull { it.skinCompatibility }.average() else null
        val averageDurability = if (reviews.any { it.durability != null }) reviews.mapNotNull { it.durability }.average() else null
        val averageDesign = if (reviews.any { it.design != null }) reviews.mapNotNull { it.design }.average() else null
        val averageFunctionality = if (reviews.any { it.functionality != null }) reviews.mapNotNull { it.functionality }.average() else null
        val averagePerformance = if (reviews.any { it.performance != null }) reviews.mapNotNull { it.performance }.average() else null
        val averageQuality = if (reviews.any { it.quality != null }) reviews.mapNotNull { it.quality }.average() else null
        val averageComfort = if (reviews.any { it.comfort != null }) reviews.mapNotNull { it.comfort }.average() else null
        val averageEaseOfUse = if (reviews.any { it.easeOfUse != null }) reviews.mapNotNull { it.easeOfUse }.average() else null

        // Güncellenmiş ortalamaları ürün veritabanına kaydetme
        products.updateOne(
            Product::id eq productId,
            combine(
                setValue(Product::averageRating, averageRating),
                setValue(Product::averageTasteFlavor, averageTasteFlavor),
                setValue(Product::averagePricePerformance, averagePricePerformance),
                setValue(Product::averageNutritionalValue, averageNutritionalValue),
                setValue(Product::averagePortionSize, averagePortionSize),
                setValue(Product::averageEffectiveness, averageEffectiveness),
                setValue(Product::averageScent, averageScent),
                setValue(Product::averageSkinCompatibility, averageSkinCompatibility),
                setValue(Product::averageDurability, averageDurability),
                setValue(Product::averageDesign, averageDesign),
                setValue(Product::averageFunctionality, averageFunctionality),
                setValue(Product::averagePerformance, averagePerformance),
                setValue(Product::averageQuality, averageQuality),
                setValue(Product::averageComfort, averageComfort),
                setValue(Product::averageEaseOfUse, averageEaseOfUse)
            )
        )
    }


    override suspend fun getAllCategories(): List<Category> {
        val products = getAllProducts()
        return products.groupBy { it.category }.map { (category, products) ->
            Category(name = category, products = products)
        }
    }

    override suspend fun getReviewsByUserId(userId: String): List<Review> {
        return reviews.find(Review::userId eq userId).toList()
    }

    override suspend fun getCategoriesByBaseCategory(baseCategory: String): List<String> {
        return products.find(Product::baseCategory eq baseCategory)
            .toList() // Sonucu önce listeye çeviriyoruz
            .map { it.category } // Ardından map işlemini uyguluyoruz
            .distinct() // Kategorileri benzersiz hale getiriyoruz
    }
}

private fun List<Double>.averageOrNull(): Double? = if (isNotEmpty()) average() else null


