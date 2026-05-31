package ru.lakuda.dfkons.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.data.api.RetrofitInstance

data class CurrencyRate(
    val code: String,
    val name: String,
    val rate: Double,
    val previousRate: Double = 0.0,
    val nominal: Int = 1
)

sealed class CurrencyUiState {
    object Loading : CurrencyUiState()
    data class Success(
        val currencies: List<CurrencyRate>,
        val updatedAt: String?
    ) : CurrencyUiState()
    data class Error(val message: String) : CurrencyUiState()
}

class CurrencyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CurrencyUiState>(CurrencyUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val currenciesToShow = listOf(
        "USD" to "Доллар США",
        "EUR" to "Евро",
        "CNY" to "Юань",
        "GBP" to "Фунт стерлингов",
        "JPY" to "Японская иена",
        "TRY" to "Турецкая лира",
        "KZT" to "Каз. тенге",
        "UZS" to "Узбекский сум"
    )

    init {
        loadExchangeRates()
    }

    fun loadExchangeRates() {
        viewModelScope.launch {
            _uiState.value = CurrencyUiState.Loading
            try {
                val response = RetrofitInstance.api.getRates()

                val currencyList = currenciesToShow.mapNotNull { (code, name) ->
                    response.valute[code]?.let { valuteInfo ->
                        val normalizedRate = valuteInfo.value / valuteInfo.nominal
                        val normalizedPrevious = valuteInfo.previous / valuteInfo.nominal
                        CurrencyRate(
                            code = code,
                            name = name,
                            rate = normalizedRate,
                            previousRate = normalizedPrevious,
                            nominal = 1
                        )
                    }
                }.sortedBy { it.code }

                if (currencyList.isEmpty()) {
                    _uiState.value = CurrencyUiState.Error("ЦБ РФ не вернул нужные валюты")
                } else {
                    _uiState.value = CurrencyUiState.Success(
                        currencies = currencyList,
                        updatedAt = response.date
                    )
                }
            } catch (e: Exception) {
                val details = e.localizedMessage ?: e.javaClass.simpleName
                _uiState.value = CurrencyUiState.Error("Ошибка загрузки курсов: $details")
            }
        }
    }
}