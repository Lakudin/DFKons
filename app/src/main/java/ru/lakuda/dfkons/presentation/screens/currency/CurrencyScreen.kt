package ru.lakuda.dfkons.presentation.screens.currency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.lakuda.dfkons.presentation.viewmodels.CurrencyUiState
import ru.lakuda.dfkons.presentation.viewmodels.CurrencyViewModel
import ru.lakuda.dfkons.presentation.viewmodels.CurrencyRate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Курсы валют") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                IconButton(onClick = { viewModel.loadExchangeRates() }) {
                    Icon(Icons.Default.Cached, contentDescription = "Обновить курсы")
                }
            }
        )

        when (uiState) {
            is CurrencyUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CurrencyUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as CurrencyUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadExchangeRates() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            is CurrencyUiState.Success -> {
                val successState = uiState as CurrencyUiState.Success
                val currencies = successState.currencies
                val updatedAtText = formatApiDate(successState.updatedAt)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currencies) { currency ->
                        CurrencyCard(currency = currency)
                    }
                    item {
                        Text(
                            text = updatedAtText ?: "Курсы ЦБ РФ на ${dateFormat.format(Date())}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CurrencyCard(currency: CurrencyRate) {
    // Определяем цвет и стрелку тренда
    val trendColor = when {
        currency.rate > currency.previousRate -> Color(0xFF4CAF50)  // Зеленый - рост
        currency.rate < currency.previousRate -> MaterialTheme.colorScheme.error  // Красный - падение
        else -> MaterialTheme.colorScheme.onSurfaceVariant  // Серый - без изменений
    }

    val trendSymbol = when {
        currency.rate > currency.previousRate -> "▲"
        currency.rate < currency.previousRate -> "▼"
        else -> "•"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("1 %s = %.4f ₽", currency.code, currency.rate),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trendSymbol,
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("вчера: %.4f", currency.previousRate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatApiDate(rawDate: String?): String? {
    if (rawDate.isNullOrBlank()) return null
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = parser.parse(rawDate) ?: return@runCatching null

        val out = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        "Курсы ЦБ РФ на " + out.format(date)
    }.getOrNull()
}