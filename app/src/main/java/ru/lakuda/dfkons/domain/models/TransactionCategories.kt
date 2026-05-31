package ru.lakuda.dfkons.domain.models

object TransactionCategories {

    val incomeCategories = listOf(
        "Заработная плата",
        "Перевод",
        "Фриланс",
        "Подарок",
        "Инвестиции",
        "Другое"
    )

    val expenseCategories = listOf(
        "Кредит",
        "Перевод",
        "Продукты",
        "Кафе и рестораны",
        "Развлечения",
        "Транспорт",
        "Коммунальные услуги",
        "Здоровье",
        "Покупки",
        "Другое"
    )

    fun getCategories(type: TransactionType): List<String> {
        return when (type) {
            TransactionType.INCOME -> incomeCategories
            TransactionType.EXPENSE -> expenseCategories
        }
    }

    fun isValidCategory(category: String, type: TransactionType): Boolean {
        return getCategories(type).contains(category)
    }
}