package ru.lakuda.dfkons.di

import android.content.Context
import ru.lakuda.dfkons.data.database.AppDatabase
import ru.lakuda.dfkons.domain.usecases.auth.LoginUserUseCase
import ru.lakuda.dfkons.domain.usecases.auth.RegisterUserUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.AddTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.DeleteTransactionUseCase
import ru.lakuda.dfkons.domain.usecases.transactions.GetTransactionsUseCase
import ru.lakuda.dfkons.repository.AuthRepository
import ru.lakuda.dfkons.repository.AuthRepositoryImpl
import ru.lakuda.dfkons.repository.TransactionRepositoryImpl

object DependencyInjection {

    private lateinit var authRepository: AuthRepository
    private lateinit var transactionRepository: TransactionRepositoryImpl

    fun init(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val userDao = database.userDao()
        val transactionDao = database.transactionDao()

        authRepository = AuthRepositoryImpl(userDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)
    }

    val loginUseCase: LoginUserUseCase by lazy {
        LoginUserUseCase(authRepository)
    }

    val registerUseCase: RegisterUserUseCase by lazy {
        RegisterUserUseCase(authRepository)
    }

    val getTransactionsUseCase: GetTransactionsUseCase by lazy {
        GetTransactionsUseCase(transactionRepository)
    }

    val addTransactionUseCase: AddTransactionUseCase by lazy {
        AddTransactionUseCase(transactionRepository)
    }

    val deleteTransactionUseCase: DeleteTransactionUseCase by lazy {
        DeleteTransactionUseCase(transactionRepository)
    }
}