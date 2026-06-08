package ru.lakuda.dfkons.main_screen.domain

import ru.lakuda.dfkons.domain.models.User

interface UserRepository {
    suspend fun getUserByEmail(email: String): User?
    suspend fun saveUser(user: User)
    suspend fun clearCurrentUser()
}
