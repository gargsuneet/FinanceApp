package com.financeapp.data.local.dao

import androidx.room.*
import com.financeapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val categoryId: Long?,
    val total: Double
)

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY date DESC")
    fun getByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    fun getTotalByTypeAndDateRange(type: String, start: Long, end: Long): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end 
        GROUP BY categoryId
    """)
    fun getCategoryTotalsForDateRange(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%' 
        ORDER BY date DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByTypeAndDateRange(type: String, start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1")
    fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND recurringNextDate IS NOT NULL AND recurringNextDate <= :now")
    suspend fun getRecurringDue(now: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
