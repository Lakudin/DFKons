package ru.lakuda.dfkons.presentation.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.domain.models.TransactionCategories
import ru.lakuda.dfkons.domain.models.TransactionType
import ru.lakuda.dfkons.domain.usecases.transactions.AddTransactionResult
import ru.lakuda.dfkons.presentation.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    userId: Long,
    viewModel: TransactionViewModel,
    onTransactionAdded: () -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(TransactionType.EXPENSE.name) }
    var selectedDateMillis by rememberSaveable { mutableStateOf(Date().time) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showSnackbar by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val selectedDate = remember(selectedDateMillis) { Date(selectedDateMillis) }
    val selectedTypeEnum = remember(selectedType) { TransactionType.valueOf(selectedType) }

    val categories = TransactionCategories.getCategories(selectedTypeEnum)

    LaunchedEffect(selectedTypeEnum) {
        selectedCategory = ""
    }

    LaunchedEffect(Unit) {
        viewModel.addResult.collect { result ->
            isLoading = false
            when (result) {
                is AddTransactionResult.Success -> {
                    amount = ""
                    selectedCategory = ""
                    description = ""
                    selectedDateMillis = Date().time
                    errorMessage = null
                    showSnackbar = true
                    onTransactionAdded()
                }
                is AddTransactionResult.Error -> {
                    errorMessage = result.message
                }
                AddTransactionResult.InvalidAmount -> {
                    errorMessage = "Сумма должна быть больше 0"
                }
                AddTransactionResult.InvalidCategory -> {
                    errorMessage = "Выберите категорию"
                }
                AddTransactionResult.InvalidDate -> {
                    errorMessage = "Дата не может быть в будущем"
                }
                null -> { /* Нет результата */ }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.commands.receive()
            snackbarHostState.showSnackbar("Операция успешно добавлена!")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            if (error != null) {
                errorMessage = error
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                showSnackbar = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Новая операция",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeEnum == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE.name
                            errorMessage = null
                        },
                        label = { Text("Расход") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTypeEnum == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME.name
                            errorMessage = null
                        },
                        label = { Text("Доход") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        val filtered = it.filter { char ->
                            char.isDigit() || char == '.' || char == ','
                        }
                        amount = filtered.replace(',', '.')
                        errorMessage = null
                    },
                    label = { Text("Сумма (₽)") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("₽") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = errorMessage != null && amount.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null && selectedCategory.isBlank()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📅 ${dateFormatter.format(selectedDate)}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка сохранения
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            errorMessage = "Введите корректную сумму"
                            return@Button
                        }
                        if (selectedCategory.isBlank()) {
                            errorMessage = "Выберите категорию"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        viewModel.addTransaction(
                            userId = userId,
                            amount = amountValue,
                            category = selectedCategory,
                            type = selectedTypeEnum,
                            date = selectedDate,
                            description = description
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Сохранить операцию")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDateMillis = date.time
                showDatePicker = false
                errorMessage = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                    onDismissRequest()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}