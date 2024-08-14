package example.com

import com.example.productrating.product.UploadImageRequest
import example.com.data.requests.AuthRequest
import example.com.data.requests.AuthRequestSignin
import example.com.data.responses.AuthResponse
import example.com.data.user.User
import example.com.data.user.UserDataSource
import example.com.product.*
import example.com.security.hashing.HashingService
import example.com.security.hashing.SaltedHash
import example.com.security.token.TokenClaim
import example.com.security.token.TokenConfig
import example.com.security.token.TokenService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.net.URL

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Geçersiz istek formatı.")
            return@post
        }

        val areFieldsBlank = request.email.isBlank() || request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 7
        val isPwTooLong = request.password.length > 64
        val hasDigit = request.password.any { it.isDigit() }
        val hasLetter = request.password.any { it.isLetter() }

        if (areFieldsBlank) {
            call.respond(HttpStatusCode.Conflict, "Alanlar boş olamaz.")
            return@post
        }

        if (isPwTooShort || isPwTooLong) {
            call.respond(HttpStatusCode.Conflict, "Şifre 7-64 karakter arası olmalıdır.")
            return@post
        }

        if (!hasDigit || !hasLetter) {
            call.respond(HttpStatusCode.Conflict, "Şifre hem harf hem de rakam içermelidir.")
            return@post
        }

        val existingUser = userDataSource.getUserByUsernameOrEmail(request.username)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "Kullanıcı adı veya email zaten mevcut.")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            email = request.email,
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.addUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict, "Kullanıcı oluşturulamadı.")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim("userId", user.id.toString()),
            TokenClaim("username", user.username)
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                userId = user.id.toString() // Kullanıcı kimliğini döndürme
            )
        )
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<AuthRequestSignin>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsernameOrEmail(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                userId = user.id.toString() // Kullanıcı kimliğini döndürme
            )
        )
    }
}



fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}


fun Route.addProduct(productDataSource: ProductDataSource) {
    authenticate {
        post("addProduct") {
            val request = call.receiveOrNull<Product>() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid request format.")
                return@post
            }

            // Temel kategori kontrolü
            val validBaseCategories = listOf("Gıda ve İçecekler", "Kişisel Bakım ve Kozmetik", "Ev ve Yaşam", "Elektronik ve Teknoloji", "Giyim ve Moda", "Sağlık ve Spor")
            if (request.baseCategory !in validBaseCategories) {
                call.respond(HttpStatusCode.BadRequest, "Invalid base category.")
                return@post
            }

            val wasAcknowledged = productDataSource.addProduct(request)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict, "Could not add product.")
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}





fun Route.getProducts(productDataSource: ProductDataSource) {
    get("getProducts") {
        val products = productDataSource.getAllProducts()
        call.respond(products)
    }
}

fun Route.getCategories(productDataSource: ProductDataSource) {
    get("getCategories") {
        val products = productDataSource.getAllProducts()
        val categories = products.groupBy { it.category }.map { (category, products) ->
            Category(name = category, products = products)
        }
        call.respond(categories)
    }
}

fun Route.addProductImage() {
    authenticate {
        post("uploadImage") {
            val multipart = call.receiveMultipart()
            var fileName: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()
                    File("uploads/$fileName").writeBytes(fileBytes)
                }
                part.dispose()
            }

            if (fileName != null) {
                val imageUrl = "http://localhost:8080/uploads/$fileName"
                call.respond(HttpStatusCode.OK, mapOf("imageUrl" to imageUrl))
            } else {
                call.respond(HttpStatusCode.BadRequest, "File not uploaded")
            }
        }
    }
}

fun Route.uploadImageFromUrl() {
    authenticate {
        post("uploadImageFromUrl") {
            val request = call.receiveOrNull<UploadImageRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Invalid request format.")
                return@post
            }

            val imageUrl = request.url
            val fileName = imageUrl.substringAfterLast('/')
            val imageBytes = URL(imageUrl).readBytes()

            File("uploads/$fileName").writeBytes(imageBytes)
            call.respond(HttpStatusCode.OK, mapOf("imageUrl" to "http://localhost:8080/uploads/$fileName"))
        }
    }
}

fun Route.addReview(productDataSource: ProductDataSource, userDataSource: UserDataSource) {
    authenticate {
        post("addReview") {
            val request = call.receiveOrNull<Review>() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Geçersiz istek formatı.")
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized, "Yetkilendirilmemiş erişim.")
                return@post
            }
            val user = userDataSource.getUserById(userId) ?: return@post
            val username = user.username

            // Ürün bilgisini al
            val product = productDataSource.getProductById(request.productId) ?: run {
                call.respond(HttpStatusCode.NotFound, "Ürün bulunamadı.")
                return@post
            }

            // Yorum nesnesini güncelle
            val review = request.copy(
                userId = userId,
                username = username,
                productName = product.name, // Ürün adını buradan alıyoruz
                date = System.currentTimeMillis().toString() // date alanını doğru Unix zaman damgası olarak ayarlayın
            )
            val wasAcknowledged = productDataSource.addReview(review)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict, "Yorum eklenemedi.")
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}





fun Route.likeReview(productDataSource: ProductDataSource, userDataSource: UserDataSource) {
    authenticate {
        post("likeReview/{reviewId}") {
            val reviewId = call.parameters["reviewId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Geçersiz yorum kimliği.")
                return@post
            }

            val review = productDataSource.getReviewById(reviewId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound, "Yorum bulunamadı.")
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: return@post

            val updatedLikes: Int
            val updatedLikedBy: List<String>
            val updatedDislikes = if (review.dislikedBy.contains(userId)) review.dislikes - 1 else review.dislikes
            val updatedDislikedBy = if (review.dislikedBy.contains(userId)) review.dislikedBy - userId else review.dislikedBy

            if (review.likedBy.contains(userId)) {
                updatedLikes = review.likes - 1
                updatedLikedBy = review.likedBy - userId
            } else {
                updatedLikes = review.likes + 1
                updatedLikedBy = review.likedBy + userId
            }

            val wasAcknowledged = productDataSource.updateReviewLikes(reviewId, updatedLikes, updatedLikedBy) &&
                    productDataSource.updateReviewDislikes(reviewId, updatedDislikes, updatedDislikedBy)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict, "Yorum beğenilemedi.")
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.dislikeReview(productDataSource: ProductDataSource, userDataSource: UserDataSource) {
    authenticate {
        post("dislikeReview/{reviewId}") {
            val reviewId = call.parameters["reviewId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Geçersiz yorum kimliği.")
                return@post
            }

            val review = productDataSource.getReviewById(reviewId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound, "Yorum bulunamadı.")
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: return@post

            val updatedDislikes: Int
            val updatedDislikedBy: List<String>
            val updatedLikes = if (review.likedBy.contains(userId)) review.likes - 1 else review.likes
            val updatedLikedBy = if (review.likedBy.contains(userId)) review.likedBy - userId else review.likedBy

            if (review.dislikedBy.contains(userId)) {
                updatedDislikes = review.dislikes - 1
                updatedDislikedBy = review.dislikedBy - userId
            } else {
                updatedDislikes = review.dislikes + 1
                updatedDislikedBy = review.dislikedBy + userId
            }

            val wasAcknowledged = productDataSource.updateReviewDislikes(reviewId, updatedDislikes, updatedDislikedBy) &&
                    productDataSource.updateReviewLikes(reviewId, updatedLikes, updatedLikedBy)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict, "Yorum beğenilemedi.")
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}



fun Route.getProductDetail(productDataSource: ProductDataSource) {
    get("product/{id}") {
        val productId = call.parameters["id"]
        if (productId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val product = productDataSource.getProductById(productId)
        if (product == null) {
            call.respond(HttpStatusCode.NotFound, "Ürün bulunamadı")
            return@get
        }
        val reviews = productDataSource.getReviewsByProductId(productId)
        val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
        call.respond(ProductDetailResponse(product, reviews, averageRating))
    }
}

fun Route.searchProducts(productDataSource: ProductDataSource) {
    get("search") {
        val query = call.request.queryParameters["query"] ?: ""

        if (query.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Arama sorgusu boş olamaz.")
            return@get
        }

        val allProducts = productDataSource.getAllProducts()
        val allCategories = productDataSource.getAllCategories()

        val (matchingProducts, matchingCategories) = fuzzySearch(query, allProducts, allCategories)

        if (matchingProducts.isEmpty() && matchingCategories.isEmpty()) {
            call.respond(HttpStatusCode.NotFound, "Aramanıza uygun ürün veya kategori bulunamadı.")
            return@get
        }

        val searchResponse = SearchResponse(
            products = matchingProducts,
            categories = matchingCategories
        )

        call.respond(HttpStatusCode.OK, searchResponse)
    }
}

fun Route.getUserReviews(productDataSource: ProductDataSource, userDataSource: UserDataSource) {
    authenticate {
        get("userReviews") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: return@get call.respond(HttpStatusCode.Unauthorized, "Yetkilendirilmediniz")

            val user = userDataSource.getUserById(userId) ?: return@get call.respond(HttpStatusCode.NotFound, "Kullanıcı bulunamadı")
            val reviews = productDataSource.getReviewsByUserId(userId).map { review ->
                val product = productDataSource.getProductById(review.productId) ?: return@get call.respond(HttpStatusCode.NotFound, "Ürün bulunamadı")
                review.copy(productName = product.name)
            }

            call.respond(reviews)
        }
    }
}

fun Route.getCategoriesByBaseCategory(productDataSource: ProductDataSource) {
    get("categoriesByBaseCategory/{baseCategory}") {
        val baseCategory = call.parameters["baseCategory"]
        if (baseCategory.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Temel kategori eksik.")
            return@get
        }

        val categories = productDataSource.getCategoriesByBaseCategory(baseCategory)
        call.respond(categories)
    }
}
















