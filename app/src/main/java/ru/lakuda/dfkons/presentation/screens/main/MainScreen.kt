package ru.lakuda.dfkons.presentation.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ru.lakuda.dfkons.di.DependencyInjection
import ru.lakuda.dfkons.presentation.screens.currency.CurrencyScreen
import ru.lakuda.dfkons.presentation.screens.statistics.StatisticsScreen
import ru.lakuda.dfkons.presentation.screens.transactions.AddTransactionScreen
import ru.lakuda.dfkons.presentation.viewmodels.AuthViewModel
import ru.lakuda.dfkons.presentation.viewmodels.StatisticsViewModel
import ru.lakuda.dfkons.presentation.viewmodels.StatisticsViewModelFactory
import ru.lakuda.dfkons.presentation.viewmodels.TransactionViewModel
import ru.lakuda.dfkons.presentation.viewmodels.TransactionViewModelFactory
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var selectedItem by rememberSaveable { mutableStateOf(0) }
    val currentUser by authViewModel.currentUser.collectAsState()

    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory()
    )
    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(
            getTransactionsUseCase = DependencyInjection.getTransactionsUseCase,
            deleteTransactionUseCase = DependencyInjection.deleteTransactionUseCase
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Финансовый планировщик")
                        if (currentUser != null) {
                            Text(
                                text = currentUser!!.username,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // ✅ КНОПКА ВЫХОДА
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Добавить") },
                    label = { Text("Операция") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Статистика") },
                    label = { Text("Статистика") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Курсы") },
                    label = { Text("Курсы валют") },
                    selected = selectedItem == 2,
                    onClick = { selectedItem = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> AddTransactionScreen(
                    userId = currentUser?.id ?: 1,
                    viewModel = transactionViewModel,
                    onTransactionAdded = { }
                )
                1 -> StatisticsScreen(
                    userId = currentUser?.id ?: 1,
                    viewModel = statisticsViewModel
                )
                2 -> CurrencyScreen()
            }
        }
    }
}