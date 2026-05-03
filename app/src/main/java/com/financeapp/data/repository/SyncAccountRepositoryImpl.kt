package com.financeapp.data.repository

import com.financeapp.data.local.dao.SyncAccountDao
import com.financeapp.data.local.entity.SyncAccountEntity
import com.financeapp.domain.model.SyncAccount
import com.financeapp.domain.repository.SyncAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SyncAccountRepositoryImpl(private val dao: SyncAccountDao) : SyncAccountRepository {
    override fun getAll(): Flow<List<SyncAccount>> = dao.getAll().map { list ->
        list.map { it.toDomain() }
    }
    override suspend fun insert(syncAccount: SyncAccount): Long = dao.insert(syncAccount.toEntity())
    override suspend fun update(syncAccount: SyncAccount) = dao.update(syncAccount.toEntity())
    override suspend fun delete(syncAccount: SyncAccount) = dao.delete(syncAccount.toEntity())

    private fun SyncAccountEntity.toDomain() = SyncAccount(id, email, name, color, isOwner)
    private fun SyncAccount.toEntity() = SyncAccountEntity(id, email, name, color, isOwner)
}
