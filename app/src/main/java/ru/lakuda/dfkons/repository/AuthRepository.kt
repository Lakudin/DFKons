package ru.lakuda.dfkons.repository

import ru.lakuda.dfkons.data.database.entities.UserEntity

interface AuthRepository {
    suspend fun login(
        loginOrEmail: String,
        passwordHash: String
    ): UserEntity?

    suspend fun register(
        username: String,
        email: String,
        passwordHash: String
    ): UserEntity?
}