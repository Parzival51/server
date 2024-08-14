package example.com.data.user

import at.favre.lib.crypto.bcrypt.BCrypt
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.or

class MongoUserDataSource(
    db: CoroutineDatabase
) : UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun addUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUserByUsernameOrEmail(usernameOrEmail: String): User? {
        return users.findOne(
            or(
                User::username eq usernameOrEmail,
                User::email eq usernameOrEmail
            )
        )
    }

    override suspend fun getUserById(userId: String): User? {
        val objectId = ObjectId(userId)
        val query: Bson = User::id eq objectId
        return users.findOne(query)
    }

    override suspend fun authenticateUser(usernameOrEmail: String, password: String): User? {
        val user = getUserByUsernameOrEmail(usernameOrEmail)
        user?.let {
            if (BCrypt.verifyer().verify(password.toCharArray(), it.password).verified) {
                return user
            }
        }
        return null
    }
}