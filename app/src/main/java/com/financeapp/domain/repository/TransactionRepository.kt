package com.financeapp.domain.repository

import com.financeapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getById(id: Long): Flow<Transaction?>
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    fun getByAccount(accountId: Long): Flow<List<Transaction>>
    fun getByCategory(categoryId: Long): Flow<List<Transaction>>
    fun getByType(type: String): Flow<List<Transaction>>
    fun getTotalByTypeAndDateRange(type: String, start: Long, end: Long): Flow<Double?>
    fun getCategoryTotalsForDateRange(start: Long, end: Long): Flow<Map<Long?, Double>>
    fun search(query: String): Flow<List<Transaction>>
    fun getByTypeAndDateRange(type: String, start: Long, end: Long): Flow<List<Transaction>>
    fun getRecurringTransactions(): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun deleteById(id: Long)
}
