package com.financeapp.data.local.dao

import androidx.room.*
import com.financeapp.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getById(id: Long): Flow<AccountEntity?>

    @Query("SELECT SUM(balance) FROM accounts WHERE includeInTotal = 1")
    fun getTotalBalance(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AccountEntity): Long

    @Update
    suspend fun update(entity: AccountEntity)

    @Delete
    suspend fun delete(entity: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun addToBalance(accountId: Long, amount: Double)

    @Query("UPDATE accounts SET balance = balance - :amount WHERE id = :accountId")
    suspend fun subtractFromBalance(accountId: Long, amount: Double)

    @Query("UPDATE accounts SET balance = :balance WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, balance: Double)
}
