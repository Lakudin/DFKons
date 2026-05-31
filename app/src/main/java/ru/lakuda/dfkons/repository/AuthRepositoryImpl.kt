package ru.lakuda.dfkons.repository

import ru.lakuda.dfkons.data.database.dao.UserDao
import ru.lakuda.dfkons.data.database.entities.UserEntity

class AuthRepositoryImpl(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(
        loginOrEmail: String,
        passwordHash: String
    ): UserEntity? {
        return try {
            userDao.login(loginOrEmail, passwordHash)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        passwordHash: String
    ): UserEntity? {
        return try {
            val exists = userDao.checkUserExists(email, username)
            if (exists != null) {
                return null
            }

            val userEntity = UserEntity(
                username = username,
                email = email,
                passwordHash = passwordHash
            )

            userDao.insertUser(userEntity)
            userDao.login(email, passwordHash)
        } catch (e: Exception) {
            null
        }
    }
}