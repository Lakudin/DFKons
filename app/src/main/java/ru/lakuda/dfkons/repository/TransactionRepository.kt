package ru.lakuda.dfkons.repository

import kotlinx.coroutines.flow.Flow
import ru.lakuda.dfkons.domain.models.Transaction

interface TransactionRepository {

    suspend fun addTransaction(transaction: Transaction): Long

    suspend fun deleteTransaction(transaction: Transaction)

    fun getTransactions(userId: Long): Flow<List<Transaction>>
}