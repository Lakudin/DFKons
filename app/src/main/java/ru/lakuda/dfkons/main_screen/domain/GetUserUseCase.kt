package ru.lakuda.dfkons.main_screen.domain

import ru.lakuda.dfkons.domain.models.User

interface GetUserUseCase {
    // Для поиска по ID (Long)
    // suspend operator fun invoke(userId: Long): User?
    // Для поиска по email (String) - перегрузка
    suspend operator fun invoke(email: String): User?
}

// Реализация UseCase
class GetUserUseCaseImpl(
    private val userRepository: UserRepository
) : GetUserUseCase {


    // Поиск по email (String)
    override suspend fun invoke(email: String): User? {
        return userRepository.getUserByEmail(email)
    }
}


