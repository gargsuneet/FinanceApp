package com.financeapp.data.repository

import com.financeapp.data.local.dao.BudgetDao
import com.financeapp.data.local.entity.BudgetEntity
import com.financeapp.domain.model.Budget
import com.financeapp.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getAll(): Flow<List<Budget>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByMonthYear(month: Int, year: Int): Flow<List<Budget>> =
        dao.getByMonthYear(month, year).map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Budget?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByCategory(categoryId: Long): Flow<List<Budget>> =
        dao.getByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun getBudgetsWithSpending(month: Int, year: Int): Flow<List<Budget>> =
        dao.getBudgetsWithSpending(month, year).map { list ->
            list.map {
                Budget(
                    id = it.budgetId,
                    categoryId = it.categoryId,
                    amount = it.budgetAmount,
                    month = it.month,
                    year = it.year,
                    currency = it.currency,
                    spent = it.spent
                )
            }
        }

    override suspend fun insert(budget: Budget): Long = dao.insert(budget.toEntity())

    override suspend fun update(budget: Budget) = dao.update(budget.toEntity())

    override suspend fun delete(budget: Budget) = dao.delete(budget.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        categoryId = categoryId,
        amount = amount,
        month = month,
        year = year,
        currency = currency
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        categoryId = categoryId,
        amount = amount,
        month = month,
        year = year,
        currency = currency
    )
}
