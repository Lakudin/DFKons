package ru.lakuda.dfkons.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val category: String,
    val type: String,
    val date: Long,
    val description: String = "",
    val note: String = ""
)