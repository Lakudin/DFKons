package ru.lakuda.dfkons.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.domain.usecases.transactions.AddTransactionResult
import ru.lakuda.dfkons.domain.usecases.transactions.AddTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.DeleteTransactionResult
import ru.lakuda.dfkons.domain.usecases.transactions.DeleteTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.GetTransactionsUseCase

class TransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _addResult = MutableStateFlow<AddTransactionResult?>(null)
    val addResult = _addResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<DeleteTransactionResult?>(null)

    private val _isLoading = MutableStateFlow(false)

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun addTransaction(
        userId: Long,
        amount: Double,
        category: String,
        type: ru.lakuda.dfkons.domain.models.TransactionType,
        date: java.util.Date,
        description: String = "",
        note: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addResult.value = null
            _error.value = null

            val result = addTransactionUseCase(
                userId = userId,
                amount = amount,
                category = category,
                type = type,
                date = date,
                description = description,
                note = note
            )

            _addResult.value = result
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}