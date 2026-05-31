package ru.lakuda.dfkons.main_screen.domain

import ru.lakuda.dfkons.domain.models.User

interface UserRepository {
    suspend fun getUserByEmail(email: String): User?
    suspend fun saveUser(user: User)
    suspend fun clearCurrentUser()
}

// Временная реализация с хранением в памяти
class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()
    private var currentUser: User? = null

    override suspend fun getUserByEmail(email: String): User? {
        return users[email.lowercase()]
    }

    override suspend fun saveUser(user: User) {
        users[user.email.lowercase()] = user
    }

    override suspend fun clearCurrentUser() {
        currentUser = null
    }
}
