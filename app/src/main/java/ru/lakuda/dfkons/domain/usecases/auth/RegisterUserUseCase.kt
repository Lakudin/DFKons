package ru.lakuda.dfkons.domain.usecases.auth

import ru.lakuda.dfkons.domain.models.User
import ru.lakuda.dfkons.domain.utils.PasswordHasher
import ru.lakuda.dfkons.repository.AuthRepository

sealed class RegisterResult {
    data class Success(val user: User) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
    object UserAlreadyExists : RegisterResult()
}

class RegisterUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): RegisterResult {
        return try {
            // Валидация
            if (username.isBlank()) {
                return RegisterResult.Error("Имя пользователя не может быть пустым")
            }
            if (email.isBlank()) {
                return RegisterResult.Error("Email не может быть пустым")
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return RegisterResult.Error("Неверный формат email")
            }
            if (password.length < 6) {
                return RegisterResult.Error("Пароль должен содержать минимум 6 символов")
            }

            val passwordHash = PasswordHasher.hash(password)
            val userEntity = authRepository.register(username, email, passwordHash)

            if (userEntity != null) {
                // Преобразуем UserEntity в User
                RegisterResult.Success(
                    User(
                        id = userEntity.id,
                        username = userEntity.username,
                        email = userEntity.email,
                        passwordHash = userEntity.passwordHash
                    )
                )
            } else {
                RegisterResult.UserAlreadyExists
            }
        } catch (e: Exception) {
            RegisterResult.Error(e.message ?: "Ошибка при регистрации")
        }
    }
}