package com.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.data.local.dao.AccountDao
import com.financeapp.data.local.dao.BudgetDao
import com.financeapp.data.local.dao.CategoryDao
import com.financeapp.data.local.dao.TransactionDao
import com.financeapp.data.local.entity.AccountEntity
import com.financeapp.data.local.entity.BudgetEntity
import com.financeapp.data.local.entity.CategoryEntity
import com.financeapp.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "finance_database"

        val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDefaultCategories(db)
                seedDefaultAccounts(db)
                seedSampleTransactions(db)
            }

            private fun seedDefaultCategories(db: SupportSQLiteDatabase) {
                val categories = listOf(
                    // Expense categories
                    Triple("Food & Dining", "EXPENSE", "restaurant"),
                    Triple("Transportation", "EXPENSE", "directions_car"),
                    Triple("Shopping", "EXPENSE", "shopping_cart"),
                    Triple("Entertainment", "EXPENSE", "movie"),
                    Triple("Bills & Utilities", "EXPENSE", "receipt"),
                    Triple("Health & Medical", "EXPENSE", "local_hospital"),
                    Triple("Education", "EXPENSE", "school"),
                    Triple("Travel", "EXPENSE", "flight"),
                    Triple("Personal Care", "EXPENSE", "spa"),
                    Triple("Home", "EXPENSE", "home"),
                    Triple("Sports & Fitness", "EXPENSE", "fitness_center"),
                    Triple("Gifts & Donations", "EXPENSE", "card_giftcard"),
                    Triple("Other Expense", "EXPENSE", "more_horiz"),
                    // Income categories
                    Triple("Salary", "INCOME", "work"),
                    Triple("Freelance", "INCOME", "laptop"),
                    Triple("Investment", "INCOME", "trending_up"),
                    Triple("Rental Income", "INCOME", "apartment"),
                    Triple("Gift", "INCOME", "redeem"),
                    Triple("Other Income", "INCOME", "attach_money")
                )
                val colors = listOf(
                    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
                    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
                    "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
                    "#FF9800", "#FF5722", "#795548", "#607D8B",
                    "#F06292", "#AED581", "#4DB6AC"
                )
                categories.forEachIndexed { index, (name, type, icon) ->
                    val color = colors[index % colors.size]
                    db.execSQL(
                        "INSERT INTO categories (name, type, icon, color, isDefault) VALUES (?, ?, ?, ?, 1)",
                        arrayOf(name, type, icon, color)
                    )
                }
            }

            private fun seedDefaultAccounts(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT INTO accounts (name, type, balance, currency, color, icon, includeInTotal) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("Cash", "CASH", 500.0, "USD", "#4CAF50", "payments", 1)
                )
                db.execSQL(
                    "INSERT INTO accounts (name, type, balance, currency, color, icon, includeInTotal) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("Bank Account", "BANK", 2500.0, "USD", "#2196F3", "account_balance", 1)
                )
                db.execSQL(
                    "INSERT INTO accounts (name, type, balance, currency, color, icon, includeInTotal, creditLimit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("Credit Card", "CREDIT_CARD", -150.0, "USD", "#F44336", "credit_card", 1, 5000.0)
                )
            }

            private fun seedSampleTransactions(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                val day = 86400000L
                db.execSQL(
                    "INSERT INTO transactions (type, amount, accountId, categoryId, note, date) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf("INCOME", 3000.0, 2, 14, "Monthly salary", now - day * 5)
                )
                db.execSQL(
                    "INSERT INTO transactions (type, amount, accountId, categoryId, note, date, fee) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("EXPENSE", 45.50, 1, 1, "Grocery shopping", now - day * 3, 0.0)
                )
                db.execSQL(
                    "INSERT INTO transactions (type, amount, accountId, categoryId, note, date) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf("EXPENSE", 120.0, 2, 5, "Electric bill", now - day * 2)
                )
                db.execSQL(
                    "INSERT INTO transactions (type, amount, accountId, categoryId, note, date, points) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("EXPENSE", 89.99, 3, 3, "Online shopping", now - day, 90.0)
                )
                db.execSQL(
                    "INSERT INTO transactions (type, amount, accountId, toAccountId, note, date) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf("TRANSFER", 500.0, 2, 1, "Cash withdrawal", now - day * 4)
                )
            }
        }
    }
}
