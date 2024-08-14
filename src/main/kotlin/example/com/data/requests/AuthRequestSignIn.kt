package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestSignin(
    val username: String,
    val password: String
)
