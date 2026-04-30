package com.financeapp.domain.repository

import com.financeapp.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getById(id: Long): Flow<Category?>
    fun getByType(type: String): Flow<List<Category>>
    fun getTopLevel(): Flow<List<Category>>
    suspend fun insert(category: Category): Long
    suspend fun insertAll(categories: List<Category>)
    suspend fun update(category: Category)
    suspend fun delete(category: Category)
    suspend fun deleteById(id: Long)
    suspend fun count(): Int
}
