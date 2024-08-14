package example.com.data.user


interface UserDataSource {
    suspend fun addUser(user: User): Boolean
    suspend fun getUserByUsernameOrEmail(usernameOrEmail: String): User?
    suspend fun authenticateUser(usernameOrEmail: String, password: String): User?
    suspend fun getUserById(userId: String): User?
}