package com.financeapp.domain.repository

import com.financeapp.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAll(): Flow<List<Budget>>
    fun getByMonthYear(month: Int, year: Int): Flow<List<Budget>>
    fun getById(id: Long): Flow<Budget?>
    fun getByCategory(categoryId: Long): Flow<List<Budget>>
    fun getBudgetsWithSpending(month: Int, year: Int): Flow<List<Budget>>
    suspend fun insert(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
    suspend fun deleteById(id: Long)
}
