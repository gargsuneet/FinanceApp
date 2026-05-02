package com.financeapp.data.repository

import com.financeapp.data.local.dao.CategoryDao
import com.financeapp.data.local.entity.CategoryEntity
import com.financeapp.domain.model.Category
import com.financeapp.domain.model.CategoryType
import com.financeapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl (
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Category?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByType(type: String): Flow<List<Category>> =
        dao.getByType(type).map { list -> list.map { it.toDomain() } }

    override fun getTopLevel(): Flow<List<Category>> =
        dao.getTopLevel().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(category: Category): Long = dao.insert(category.toEntity())

    override suspend fun insertAll(categories: List<Category>) =
        dao.insertAll(categories.map { it.toEntity() })

    override suspend fun update(category: Category) = dao.update(category.toEntity())

    override suspend fun delete(category: Category) = dao.delete(category.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun count(): Int = dao.count()

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        type = CategoryType.valueOf(type),
        icon = icon,
        color = color,
        isDefault = isDefault,
        parentId = parentId
    )

    private fun Category.toEntity() = CategoryEntity(
        id = id,
        name = name,
        type = type.name,
        icon = icon,
        color = color,
        isDefault = isDefault,
        parentId = parentId
    )
}
