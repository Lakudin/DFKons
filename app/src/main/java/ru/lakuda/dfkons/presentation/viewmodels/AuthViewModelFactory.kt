package ru.lakuda.dfkons.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.lakuda.dfkons.domain.usecases.auth.LoginUserUseCase
import ru.lakuda.dfkons.domain.usecases.auth.RegisterUserUseCase

class AuthViewModelFactory(
    private val loginUseCase: LoginUserUseCase,
    private val registerUseCase: RegisterUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(loginUseCase, registerUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}