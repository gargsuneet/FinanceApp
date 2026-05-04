package com.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SyncAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["syncAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("accountId"), Index("toAccountId"), Index("categoryId"), Index("syncAccountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val fee: Double = 0.0,
    val points: Double = 0.0,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null,
    val currency: String = "USD",
    val syncAccountId: Long? = null,
    val photoUri: String? = null,
    val recurringNextDate: Long? = null
)
