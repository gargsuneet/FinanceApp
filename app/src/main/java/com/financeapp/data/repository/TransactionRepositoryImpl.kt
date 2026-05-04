package com.financeapp.data.repository

import com.financeapp.data.local.dao.TransactionDao
import com.financeapp.data.local.entity.TransactionEntity
import com.financeapp.domain.model.RecurringPeriod
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.model.TransactionType
import com.financeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl (
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Transaction?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        dao.getByDateRange(start, end).map { list -> list.map { it.toDomain() } }

    override fun getByAccount(accountId: Long): Flow<List<Transaction>> =
        dao.getByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(categoryId: Long): Flow<List<Transaction>> =
        dao.getByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun getByType(type: String): Flow<List<Transaction>> =
        dao.getByType(type).map { list -> list.map { it.toDomain() } }

    override fun getTotalByTypeAndDateRange(type: String, start: Long, end: Long): Flow<Double?> =
        dao.getTotalByTypeAndDateRange(type, start, end)

    override fun getCategoryTotalsForDateRange(start: Long, end: Long): Flow<Map<Long?, Double>> =
        dao.getCategoryTotalsForDateRange(start, end).map { list ->
            list.associate { it.categoryId to it.total }
        }

    override fun search(query: String): Flow<List<Transaction>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override fun getByTypeAndDateRange(type: String, start: Long, end: Long): Flow<List<Transaction>> =
        dao.getByTypeAndDateRange(type, start, end).map { list -> list.map { it.toDomain() } }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        dao.getRecurringTransactions().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) =
        dao.update(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) =
        dao.delete(transaction.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        amount = amount,
        fee = fee,
        points = points,
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = note,
        date = date,
        isRecurring = isRecurring,
        recurringPeriod = recurringPeriod?.let { RecurringPeriod.valueOf(it) },
        currency = currency,
        syncAccountId = syncAccountId,
        photoUri = photoUri
    ) = TransactionEntity(
        id = id,
        type = type.name,
        amount = amount,
        fee = fee,
        points = points,
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = note,
        date = date,
        isRecurring = isRecurring,
        recurringPeriod = recurringPeriod?.name,
        currency = currency,
        syncAccountId = syncAccountId,
        photoUri = photoUri
    )
}
