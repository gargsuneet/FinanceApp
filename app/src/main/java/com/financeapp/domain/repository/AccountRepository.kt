package com.financeapp.domain.repository

import com.financeapp.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAll(): Flow<List<Account>>
    fun getById(id: Long): Flow<Account?>
    fun getTotalBalance(): Flow<Double?>
    suspend fun insert(account: Account): Long
    suspend fun update(account: Account)
    suspend fun delete(account: Account)
    suspend fun deleteById(id: Long)
    suspend fun addToBalance(accountId: Long, amount: Double)
    suspend fun subtractFromBalance(accountId: Long, amount: Double)
    suspend fun updateBalance(accountId: Long, balance: Double)
}
