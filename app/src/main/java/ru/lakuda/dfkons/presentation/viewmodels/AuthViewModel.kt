package ru.lakuda.dfkons.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.lakuda.dfkons.domain.models.User
import ru.lakuda.dfkons.domain.usecases.auth.LoginResult
import ru.lakuda.dfkons.domain.usecases.auth.LoginUserUseCase
import ru.lakuda.dfkons.domain.usecases.auth.RegisterResult
import ru.lakuda.dfkons.domain.usecases.auth.RegisterUserUseCase

class AuthViewModel(
    private val loginUseCase: LoginUserUseCase,
    private val registerUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult = _loginResult.asStateFlow()

    private val _registerResult = MutableStateFlow<RegisterResult?>(null)
    val registerResult = _registerResult.asStateFlow()

    fun login(loginOrEmail: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase(loginOrEmail, password)
            _loginResult.value = result

            when (result) {
                is LoginResult.Success -> {
                    _currentUser.value = result.user
                    _isLoggedIn.value = true
                }
                else -> { }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = registerUseCase(username, email, password)
            _registerResult.value = result

            when (result) {
                is RegisterResult.Success -> {
                    _currentUser.value = result.user
                    _isLoggedIn.value = true
                }
                else -> {}
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _loginResult.value = null
        _registerResult.value = null
    }
}