package com.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val balance: Double = 0.0,
    val currency: String = "USD",
    val color: String = "#2196F3",
    val icon: String = "account_balance_wallet",
    val includeInTotal: Boolean = true,
    val creditLimit: Double = 0.0
)
