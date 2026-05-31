package ru.lakuda.dfkons.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.lakuda.dfkons.data.database.dao.TransactionDao
import ru.lakuda.dfkons.data.database.entities.TransactionEntity
import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.domain.models.TransactionType
import java.util.Date

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun addTransaction(transaction: Transaction): Long {
        return dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    override fun getTransactions(userId: Long): Flow<List<Transaction>> {
        return dao.getTransactions(userId).map { list ->
            list.map { it.toDomain() }
        }
    }
}

private fun Transaction.toEntity() = TransactionEntity(
    id = id,
    userId = userId,
    amount = amount,
    category = category,
    type = if (type == TransactionType.INCOME) "income" else "expense",
    date = date.time,
    description = description,
    note = note
)

private fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    amount = amount,
    category = category,
    type = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
    date = Date(date),
    description = description,
    note = note
)