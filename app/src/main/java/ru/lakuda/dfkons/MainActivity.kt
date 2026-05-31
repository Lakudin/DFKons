package ru.lakuda.dfkons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.lakuda.dfkons.di.DependencyInjection
import ru.lakuda.dfkons.presentation.screens.auth.LoginScreen
import ru.lakuda.dfkons.presentation.screens.auth.RegisterScreen
import ru.lakuda.dfkons.presentation.screens.main.MainScreen
import ru.lakuda.dfkons.presentation.viewmodels.AuthViewModel
import ru.lakuda.dfkons.presentation.viewmodels.AuthViewModelFactory
import ru.lakuda.dfkons.ui.theme.DFKonsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DependencyInjection.init(applicationContext)

        setContent {
            DFKonsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            loginUseCase = DependencyInjection.loginUseCase,
            registerUseCase = DependencyInjection.registerUseCase
        )
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("main") {
            MainScreen(navController, authViewModel)
        }
    }
}