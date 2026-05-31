package ru.lakuda.dfkons.domain.usecases.transactions

import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.repository.TransactionRepository

sealed class DeleteTransactionResult {
    object Success : DeleteTransactionResult()
    data class Error(val message: String) : DeleteTransactionResult()
}

class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): DeleteTransactionResult {
        return try {
            repository.deleteTransaction(transaction)
            DeleteTransactionResult.Success
        } catch (e: Exception) {
            DeleteTransactionResult.Error(e.message ?: "Ошибка при удалении")
        }
    }
}