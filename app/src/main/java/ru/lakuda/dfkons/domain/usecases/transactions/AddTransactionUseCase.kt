package ru.lakuda.dfkons.domain.usecases.transactions

import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.domain.models.TransactionCategories
import ru.lakuda.dfkons.repository.TransactionRepository
import java.util.Date

sealed class AddTransactionResult {
    data class Success(val transactionId: Long) : AddTransactionResult()
    data class Error(val message: String) : AddTransactionResult()
    object InvalidAmount : AddTransactionResult()
    object InvalidCategory : AddTransactionResult()
    object InvalidDate : AddTransactionResult()
}

class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        userId: Long,
        amount: Double,
        category: String,
        type: ru.lakuda.dfkons.domain.models.TransactionType,
        date: Date,
        description: String = "",
        note: String = ""
    ): AddTransactionResult {
        return try {
            // Валидация суммы
            if (amount <= 0) {
                return AddTransactionResult.InvalidAmount
            }

            // Валидация категории
            if (!TransactionCategories.isValidCategory(category, type)) {
                return AddTransactionResult.InvalidCategory
            }

            // Валидация даты
            if (date.after(Date())) {
                return AddTransactionResult.InvalidDate
            }

            val transaction = Transaction(
                userId = userId,
                amount = amount,
                category = category,
                type = type,
                date = date,
                description = description,
                note = note
            )

            val transactionId = repository.addTransaction(transaction)

            if (transactionId > 0) {
                AddTransactionResult.Success(transactionId)
            } else {
                AddTransactionResult.Error("Ошибка при сохранении транзакции")
            }
        } catch (e: Exception) {
            AddTransactionResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}