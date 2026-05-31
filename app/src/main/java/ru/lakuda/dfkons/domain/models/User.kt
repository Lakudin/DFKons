package ru.lakuda.dfkons.domain.models

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String = ""
)