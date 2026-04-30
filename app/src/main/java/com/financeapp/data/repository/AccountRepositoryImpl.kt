package com.financeapp.data.repository

import com.financeapp.data.local.dao.AccountDao
import com.financeapp.data.local.entity.AccountEntity
import com.financeapp.domain.model.Account
import com.financeapp.domain.model.AccountType
import com.financeapp.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao
) : AccountRepository {

    override fun getAll(): Flow<List<Account>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Account?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getTotalBalance(): Flow<Double?> = dao.getTotalBalance()

    override suspend fun insert(account: Account): Long = dao.insert(account.toEntity())

    override suspend fun update(account: Account) = dao.update(account.toEntity())

    override suspend fun delete(account: Account) = dao.delete(account.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun addToBalance(accountId: Long, amount: Double) =
        dao.addToBalance(accountId, amount)

    override suspend fun subtractFromBalance(accountId: Long, amount: Double) =
        dao.subtractFromBalance(accountId, amount)

    override suspend fun updateBalance(accountId: Long, balance: Double) =
        dao.updateBalance(accountId, balance)

    private fun AccountEntity.toDomain() = Account(
        id = id,
        name = name,
        type = AccountType.valueOf(type),
        balance = balance,
        currency = currency,
        color = color,
        icon = icon,
        includeInTotal = includeInTotal,
        creditLimit = creditLimit
    )

    private fun Account.toEntity() = AccountEntity(
        id = id,
        name = name,
        type = type.name,
        balance = balance,
        currency = currency,
        color = color,
        icon = icon,
        includeInTotal = includeInTotal,
        creditLimit = creditLimit
    )
}
