package ru.lakuda.dfkons.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.domain.models.TransactionType
import ru.lakuda.dfkons.domain.usecases.transactions.DeleteTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.GetTransactionsUseCase
import java.text.SimpleDateFormat
import java.util.*

data class StatisticsData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val dailyTransactions: Map<String, Pair<Double, Double>> = emptyMap(), // date -> (expense, income)
    val categoryExpenses: Map<String, Double> = emptyMap(),
    val topExpenseCategories: List<Pair<String, Double>> = emptyList(),
    val transactionsCount: Int = 0,
    val averageDailyExpense: Double = 0.0
)

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val data: StatisticsData) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var currentStartDate: Date? = null
    private var currentEndDate: Date? = null
    private var currentUserId: Long = 0

    fun loadTransactions(userId: Long) {
        currentUserId = userId
        viewModelScope.launch {
            getTransactionsUseCase(userId).collectLatest { transactions ->
                _transactions.value = transactions
                allTransactions = transactions
                updateStatistics()
            }
        }
    }

    fun loadStatistics(userId: Long, startDate: Date? = null, endDate: Date? = null) {
        currentUserId = userId
        currentStartDate = startDate
        currentEndDate = endDate
        updateStatistics()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
            loadTransactions(currentUserId)
            updateStatistics()
        }
    }

    private fun updateStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading

            try {
                val filteredTransactions = filterTransactionsByDate(allTransactions, currentStartDate, currentEndDate)
                val statistics = calculateStatistics(filteredTransactions)
                _uiState.value = StatisticsUiState.Success(statistics)
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Ошибка загрузки статистики")
            }
        }
    }

    private fun filterTransactionsByDate(
        transactions: List<Transaction>,
        startDate: Date?,
        endDate: Date?
    ): List<Transaction> {
        return transactions.filter { transaction ->
            var include = true
            startDate?.let {
                if (transaction.date.before(it)) include = false
            }
            endDate?.let {
                if (transaction.date.after(it)) include = false
            }
            include
        }
    }

    private fun calculateStatistics(transactions: List<Transaction>): StatisticsData {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val balance = totalIncome - totalExpense

        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        val dailyTransactions = mutableMapOf<String, Pair<Double, Double>>()

        transactions.forEach { transaction ->
            val dateKey = dateFormat.format(transaction.date)
            val current = dailyTransactions[dateKey] ?: (0.0 to 0.0)

            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    dailyTransactions[dateKey] = (current.first + transaction.amount) to current.second
                }
                TransactionType.INCOME -> {
                    dailyTransactions[dateKey] = current.first to (current.second + transaction.amount)
                }
            }
        }

        val categoryExpenses = mutableMapOf<String, Double>()
        transactions.filter { it.type == TransactionType.EXPENSE }
            .forEach { transaction ->
                categoryExpenses[transaction.category] =
                    (categoryExpenses[transaction.category] ?: 0.0) + transaction.amount
            }

        val topExpenseCategories = categoryExpenses.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        val uniqueDays = dailyTransactions.keys.size
        val averageDailyExpense = if (uniqueDays > 0) totalExpense / uniqueDays else 0.0

        return StatisticsData(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            dailyTransactions = dailyTransactions,
            categoryExpenses = categoryExpenses,
            topExpenseCategories = topExpenseCategories,
            transactionsCount = transactions.size,
            averageDailyExpense = averageDailyExpense
        )
    }
}