package example.com

import example.com.data.user.MongoUserDataSource
import example.com.plugins.*
import example.com.product.MongoProductDataSource
import example.com.security.hashing.SHA256HashingService
import example.com.security.token.JwtTokenService
import example.com.security.token.TokenConfig
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val dbName = "ktor-product-rating"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://yusufemreai51:l82rL2r8TuyARnvA@productrating.wpjwteh.mongodb.net/?retryWrites=true&w=majority&appName=productRating&ssl=true&sslInvalidHostNameAllowed=true
"
    ).coroutine
        .getDatabase(dbName)
    val productDataSource = MongoProductDataSource(db)
    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureRouting(userDataSource, productDataSource, hashingService, tokenService, tokenConfig)
    configureSerialization()
    configureMonitoring()

    // Ensure uploads directory exists
    File("uploads").mkdir()
}




