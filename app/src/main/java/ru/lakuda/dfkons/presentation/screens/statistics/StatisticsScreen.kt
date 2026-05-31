package ru.lakuda.dfkons.presentation.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.domain.models.Transaction
import ru.lakuda.dfkons.domain.models.TransactionType
import ru.lakuda.dfkons.presentation.components.LineChart
import ru.lakuda.dfkons.presentation.components.PieChart
import ru.lakuda.dfkons.presentation.viewmodels.StatisticsData
import ru.lakuda.dfkons.presentation.viewmodels.StatisticsUiState
import ru.lakuda.dfkons.presentation.viewmodels.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun adaptiveSp(fontSize: Int): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    return when {
        screenWidthDp < 360 -> (fontSize - 4).sp
        screenWidthDp < 400 -> (fontSize - 2).sp
        else -> fontSize.sp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    userId: Long,
    viewModel: StatisticsViewModel
) {
    var startDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var endDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTransactionId by rememberSaveable { mutableStateOf<Long?>(null) }
    val startDate = remember(startDateMillis) { startDateMillis?.let { Date(it) } }
    val endDate = remember(endDateMillis) { endDateMillis?.let { Date(it) } }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val selectedTransaction = remember(transactions, selectedTransactionId) {
        transactions.firstOrNull { it.id == selectedTransactionId }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics(userId)
        viewModel.loadTransactions(userId)
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(snackbarMessage)
                showSnackbar = false
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is StatisticsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is StatisticsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (uiState as StatisticsUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadStatistics(userId, startDate, endDate) }) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                is StatisticsUiState.Success -> {
                    val data = (uiState as StatisticsUiState.Success).data
                    StatisticsContent(
                        data = data,
                        transactions = transactions,
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateClick = { showStartPicker = true },
                        onEndDateClick = { showEndPicker = true },
                        onApplyFilter = { viewModel.loadStatistics(userId, startDate, endDate) },
                        onDeleteTransaction = { transaction ->
                            selectedTransactionId = transaction.id
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            onDateSelected = { date ->
                startDateMillis = date.time
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            onDateSelected = { date ->
                endDateMillis = date.time
                showEndPicker = false
            }
        )
    }

    if (showDeleteDialog && selectedTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить операцию") },
            text = {
                Text(
                    "Вы уверены, что хотите удалить операцию на сумму ${
                        String.format("%.2f", selectedTransaction?.amount ?: 0.0)
                    } ₽?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTransaction?.let {
                            viewModel.deleteTransaction(it)
                            viewModel.loadStatistics(userId, startDate, endDate)
                            viewModel.loadTransactions(userId)
                            snackbarMessage = "Операция удалена"
                            showSnackbar = true
                        }
                        showDeleteDialog = false
                        selectedTransactionId = null
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun StatisticsContent(
    data: StatisticsData,
    transactions: List<Transaction>,
    startDate: Date?,
    endDate: Date?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onApplyFilter: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    val sortedTransactions = transactions.sortedByDescending { it.date }
    val filteredForGraph = transactions.filter { tx ->
        var include = true
        startDate?.let { if (tx.date.before(it.atStartOfDay())) include = false }
        endDate?.let { if (tx.date.after(it.atEndOfDay())) include = false }
        include
    }
    //val errorColor = MaterialTheme.colorScheme.error


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { FilterCard(startDate, endDate, onStartDateClick, onEndDateClick, onApplyFilter) }
        item { StatisticsSummaryCard(data.totalIncome, data.totalExpense, data.balance) }
        item { AdditionalInfoCard(data.transactionsCount, data.averageDailyExpense) }

        if (filteredForGraph.isNotEmpty()) {
            val graphTx = filteredForGraph.sortedBy { it.date }

            val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val dayLabelFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

            val startCal = Calendar.getInstance().apply {
                time = (startDate ?: graphTx.first().date).atStartOfDay()
            }
            val endCal = Calendar.getInstance().apply {
                time = (endDate ?: graphTx.last().date).atStartOfDay()
            }

            val deltaByDay: Map<String, Double> = graphTx
                .groupBy { dayKeyFormat.format(it.date.atStartOfDay()) }
                .mapValues { (_, dayTx) ->
                    dayTx.sumOf { tx ->
                        if (tx.type == TransactionType.INCOME) tx.amount else -tx.amount
                    }
                }

            var cursor = startCal.clone() as Calendar
            var balance = 0.0
            val labels = mutableListOf<String>()
            val points = mutableListOf<Float>()

            while (!cursor.after(endCal)) {
                val key = dayKeyFormat.format(cursor.time)
                balance += deltaByDay[key] ?: 0.0
                labels.add(dayLabelFormat.format(cursor.time))
                points.add(balance.toFloat())
                cursor.add(Calendar.DAY_OF_MONTH, 1)
            }

            val lineColor = Color(0xFF26A69A) // бирюзовый, как на макете
            val areaColor = lineColor.copy(alpha = 0.18f)

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Динамика доходов и расходов",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Показываем, как менялся баланс (доходы − расходы) за период",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        LineChart(
                            data = points,
                            labels = labels,
                            lineColor = lineColor,
                            areaColor = areaColor,
                            pointColor = lineColor,
                            showPoints = false,
                            stepLine = false,
                            smoothLine = true,
                            yAxisFromZero = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (data.categoryExpenses.isNotEmpty()) {
            item { CategoryPieChartCard(data.categoryExpenses) }
        }

        if (data.topExpenseCategories.isNotEmpty()) {
            item { TopCategoriesCard(data.topExpenseCategories) }
        }

        if (transactions.isNotEmpty()) {
            item {
                Text(
                    text = "Последние операции",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(sortedTransactions) { transaction ->
                TransactionItem(transaction, onDelete = { onDeleteTransaction(transaction) })
            }
        } else {
            item { EmptyStateCard() }
        }
    }
}

@Composable
fun FilterCard(
    startDate: Date?,
    endDate: Date?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onApplyFilter: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Фильтр по периоду", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(startDate?.let { dateFormat.format(it) } ?: "Начало", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(endDate?.let { dateFormat.format(it) } ?: "Конец", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onApplyFilter,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Применить фильтр")
            }
        }
    }
}

@Composable
fun StatisticsSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticCard(
                    title = "Доходы",
                    value = formatCurrency(totalIncome),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                StatisticCard(
                    title = "Расходы",
                    value = formatCurrency(totalExpense),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            StatisticCard(
                title = "Баланс",
                value = formatCurrency(balance),
                color = if (balance >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AdditionalInfoCard(transactionsCount: Int, averageDailyExpense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("Операций", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$transactionsCount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("Ср. расход/день", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatCurrency(averageDailyExpense), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryPieChartCard(categoryExpenses: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Расходы по категориям", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            PieChart(data = categoryExpenses, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TopCategoriesCard(topCategories: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Топ расходов", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            topCategories
                .take(4)
                .forEach { (category, amount) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(category)
                    Text(
                        String.format("%,.2f ₽", amount),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val isIncome = transaction.type == TransactionType.INCOME

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isIncome) Color(0xFF4CAF50).copy(0.15f) else MaterialTheme.colorScheme.error.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Medium)
                Text(
                    transaction.description.ifEmpty { "Без описания" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(dateFormat.format(transaction.date), style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.2f ₽", transaction.amount),
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Нет данных", style = MaterialTheme.typography.titleMedium)
            Text("Добавьте операции для отображения статистики", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(Date(it)) }
                    onDismissRequest()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Отмена") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatCurrency(amount: Double): String = String.format("%,.2f ₽", amount)

private fun Date.atStartOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

private fun Date.atEndOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
}
