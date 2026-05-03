package com.financeapp.domain.model

enum class TransactionType { INCOME, EXPENSE, TRANSFER }
enum class AccountType { CASH, BANK, CREDIT_CARD, SAVINGS, INVESTMENT, OTHER }
enum class CategoryType { INCOME, EXPENSE, BOTH }
enum class RecurringPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val fee: Double = 0.0,
    val points: Double = 0.0,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val currency: String = "USD",
    val syncAccountId: Long? = null,
    val syncAccountName: String = "",
    // Resolved display fields
    val accountName: String = "",
    val toAccountName: String = "",
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = ""
)

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0,
    val currency: String = "USD",
    val color: String = "#2196F3",
    val icon: String = "account_balance_wallet",
    val includeInTotal: Boolean = true,
    val creditLimit: Double = 0.0
)

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val icon: String = "category",
    val color: String = "#9C27B0",
    val isDefault: Boolean = false,
    val parentId: Long? = null
)

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val month: Int,
    val year: Int,
    val currency: String = "USD",
    val spent: Double = 0.0,
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = ""
)

data class MonthlySummary(
    val income: Double,
    val expense: Double,
    val balance: Double = income - expense,
    val month: Int,
    val year: Int
)

data class CategorySpending(
    val categoryId: Long?,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val amount: Double,
    val percentage: Float
)

data class SyncAccount(
    val id: Long = 0,
    val email: String,
    val name: String,
    val color: String = "#2196F3",
    val isOwner: Boolean = false
)
