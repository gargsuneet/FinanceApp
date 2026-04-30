package com.financeapp.di

import com.financeapp.data.repository.AccountRepositoryImpl
import com.financeapp.data.repository.BudgetRepositoryImpl
import com.financeapp.data.repository.CategoryRepositoryImpl
import com.financeapp.data.repository.TransactionRepositoryImpl
import com.financeapp.domain.repository.AccountRepository
import com.financeapp.domain.repository.BudgetRepository
import com.financeapp.domain.repository.CategoryRepository
import com.financeapp.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}
