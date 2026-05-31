package ru.lakuda.dfkons.domain.models

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Date,
    val description: String = "",
    val note: String = ""
)