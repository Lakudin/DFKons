package ru.lakuda.dfkons.domain.usecases.auth

import ru.lakuda.dfkons.domain.models.User
import ru.lakuda.dfkons.domain.utils.PasswordHasher
import ru.lakuda.dfkons.repository.AuthRepository
import ru.lakuda.dfkons.data.database.entities.UserEntity  // ← этот импорт нужен

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object InvalidCredentials : LoginResult()
}

class LoginUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        loginOrEmail: String,
        password: String
    ): LoginResult {
        return try {
            val passwordHash = PasswordHasher.hash(password)
            val userEntity = authRepository.login(loginOrEmail, passwordHash)

            if (userEntity != null) {
                LoginResult.Success(
                    User(
                        id = userEntity.id,
                        username = userEntity.username,
                        email = userEntity.email,
                        passwordHash = userEntity.passwordHash
                    )
                )
            } else {
                LoginResult.InvalidCredentials
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Ошибка при входе")
        }
    }
}