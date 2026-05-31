package ru.lakuda.dfkons.domain.usecases.transactions

import kotlinx.coroutines.flow.Flow
import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.repository.TransactionRepository

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(userId: Long): Flow<List<Transaction>> {
        return repository.getTransactions(userId)
    }
}