package example.com.product

interface ProductDataSource {
    suspend fun addProduct(product: Product): Boolean
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
    suspend fun uploadImage(image: ByteArray, fileName: String): String
    suspend fun addReview(review: Review): Boolean
    suspend fun getReviewsByProductId(productId: String): List<Review>
    suspend fun getReviewById(reviewId: String): Review?
    suspend fun updateReviewLikes(reviewId: String, likes: Int, likedBy: List<String>): Boolean
    suspend fun updateReviewDislikes(reviewId: String, dislikes: Int, dislikedBy: List<String>): Boolean
    suspend fun getAllCategories(): List<Category>
    suspend fun getReviewsByUserId(userId: String): List<Review>
    suspend fun getCategoriesByBaseCategory(baseCategory: String): List<String>
}
