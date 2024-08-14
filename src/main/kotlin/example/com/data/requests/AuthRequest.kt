package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val username: String,
    val password: String
)