package com.financeapp.di

import android.content.Context
import androidx.room.Room
import com.financeapp.data.local.FinanceDatabase
import com.financeapp.data.local.dao.AccountDao
import com.financeapp.data.local.dao.BudgetDao
import com.financeapp.data.local.dao.CategoryDao
import com.financeapp.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, FinanceDatabase.DATABASE_NAME)
            .addCallback(FinanceDatabase.seedCallback)
            .build()

    @Provides
    fun provideTransactionDao(db: FinanceDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideAccountDao(db: FinanceDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: FinanceDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: FinanceDatabase): BudgetDao = db.budgetDao()
}
