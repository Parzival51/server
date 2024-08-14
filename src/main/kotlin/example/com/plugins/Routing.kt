package example.com.plugins

import example.com.*
import example.com.data.user.UserDataSource
import example.com.product.ProductDataSource
import example.com.security.hashing.HashingService
import example.com.security.token.TokenConfig
import example.com.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    productDataSource: ProductDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        authenticate() {
            authenticate()
            getSecretInfo()
            addProduct(productDataSource)
            addProductImage()
            uploadImageFromUrl()
            addReview(productDataSource,userDataSource)
            likeReview(productDataSource,userDataSource)
            dislikeReview(productDataSource,userDataSource)
            getUserReviews(productDataSource, userDataSource)

        }

        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource, tokenService, tokenConfig)
        getProducts(productDataSource)
        getCategories(productDataSource)
        getProductDetail(productDataSource)
        searchProducts(productDataSource)
        getCategoriesByBaseCategory(productDataSource)

    }
}

