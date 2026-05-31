package ru.lakuda.dfkons.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.lakuda.dfkons.data.database.AppDatabase
import ru.lakuda.dfkons.domain.usecases.transactions.AddTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.DeleteTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.GetTransactionsUseCase
import ru.lakuda.dfkons.repository.TransactionRepositoryImpl

class TransactionViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(
                addTransactionUseCase = AddTransactionUseCase(getTransactionRepository()),
                deleteTransactionUseCase = DeleteTransactionUseCase(getTransactionRepository()),
                getTransactionsUseCase = GetTransactionsUseCase(getTransactionRepository())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun getTransactionRepository(): TransactionRepositoryImpl {
        val database = AppDatabase.getDatabase(context)
        return TransactionRepositoryImpl(database.transactionDao())
    }
}