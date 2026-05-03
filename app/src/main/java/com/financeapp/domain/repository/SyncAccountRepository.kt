package com.financeapp.domain.repository

import com.financeapp.domain.model.SyncAccount
import kotlinx.coroutines.flow.Flow

interface SyncAccountRepository {
    fun getAll(): Flow<List<SyncAccount>>
    suspend fun insert(syncAccount: SyncAccount): Long
    suspend fun update(syncAccount: SyncAccount)
    suspend fun delete(syncAccount: SyncAccount)
}
