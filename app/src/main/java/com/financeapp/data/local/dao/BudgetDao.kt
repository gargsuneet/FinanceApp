package com.financeapp.data.local.dao

import androidx.room.*
import com.financeapp.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

data class BudgetWithSpending(
    val budgetId: Long,
    val categoryId: Long,
    val budgetAmount: Double,
    val spent: Double,
    val month: Int,
    val year: Int,
    val currency: String
)

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getByMonthYear(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getById(id: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Long): Flow<List<BudgetEntity>>

    @Query("""
        SELECT b.id as budgetId, b.categoryId, b.amount as budgetAmount,
               COALESCE(SUM(t.amount), 0) as spent,
               b.month, b.year, b.currency
        FROM budgets b
        LEFT JOIN transactions t ON t.categoryId = b.categoryId
            AND t.type = 'EXPENSE'
            AND strftime('%m', datetime(t.date/1000, 'unixepoch')) = printf('%02d', b.month)
            AND strftime('%Y', datetime(t.date/1000, 'unixepoch')) = CAST(b.year AS TEXT)
        WHERE b.month = :month AND b.year = :year
        GROUP BY b.id
    """)
    fun getBudgetsWithSpending(month: Int, year: Int): Flow<List<BudgetWithSpending>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BudgetEntity): Long

    @Update
    suspend fun update(entity: BudgetEntity)

    @Delete
    suspend fun delete(entity: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM budgets")
    fun getAll(): Flow<List<BudgetEntity>>
}
