package com.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val icon: String = "category",
    val color: String = "#9C27B0",
    val isDefault: Boolean = false,
    val parentId: Long? = null
)
