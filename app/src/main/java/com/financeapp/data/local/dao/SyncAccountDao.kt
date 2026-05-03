package com.financeapp.data.local.dao

import androidx.room.*
import com.financeapp.data.local.entity.SyncAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncAccountDao {
    @Query("SELECT * FROM sync_accounts ORDER BY name ASC")
    fun getAll(): Flow<List<SyncAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SyncAccountEntity): Long

    @Update
    suspend fun update(entity: SyncAccountEntity)

    @Delete
    suspend fun delete(entity: SyncAccountEntity)

    @Query("DELETE FROM sync_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
