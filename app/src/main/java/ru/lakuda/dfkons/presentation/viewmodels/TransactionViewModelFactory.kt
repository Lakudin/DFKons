package ru.lakuda.dfkons.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.lakuda.dfkons.di.DependencyInjection

class TransactionViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(
                addTransactionUseCase    = DependencyInjection.addTransactionUseCase,
                deleteTransactionUseCase = DependencyInjection.deleteTransactionUseCase,
                getTransactionsUseCase   = DependencyInjection.getTransactionsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}