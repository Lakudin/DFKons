package ru.lakuda.dfkons.presentation

sealed interface AppEvent {
    data object OnAuthorisationButtonClick : AppEvent
    data object OnTransactionAddButtonClick : AppEvent
    data object OnChartsShowClick : AppEvent
    data class OnChangeLoginField (val login : String) : AppEvent
    data class OnChangePasswordField (val password : String) : AppEvent
    data object OnSignInClick : AppEvent
    data object OnSingUotClick : AppEvent
    data object OnSignUpClick : AppEvent

}