package com.financeapp

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.financeapp.data.local.FinanceDatabase
import com.financeapp.data.repository.AccountRepositoryImpl
import com.financeapp.data.repository.BudgetRepositoryImpl
import com.financeapp.data.repository.CategoryRepositoryImpl
import com.financeapp.data.repository.SyncAccountRepositoryImpl
import com.financeapp.data.repository.TransactionRepositoryImpl
import com.financeapp.domain.repository.AccountRepository
import com.financeapp.domain.repository.BudgetRepository
import com.financeapp.domain.repository.CategoryRepository
import com.financeapp.domain.repository.SyncAccountRepository
import com.financeapp.domain.repository.TransactionRepository
import com.financeapp.domain.usecase.*

class FinanceApplication : Application() {

    lateinit var database: FinanceDatabase
        private set

    lateinit var transactionRepository: TransactionRepository
        private set
    lateinit var accountRepository: AccountRepository
        private set
    lateinit var categoryRepository: CategoryRepository
        private set
    lateinit var budgetRepository: BudgetRepository
        private set
    lateinit var syncAccountRepository: SyncAccountRepository
        private set

    lateinit var getTransactionsUseCase: GetTransactionsUseCase
        private set
    lateinit var addTransactionUseCase: AddTransactionUseCase
        private set
    lateinit var updateTransactionUseCase: UpdateTransactionUseCase
        private set
    lateinit var deleteTransactionUseCase: DeleteTransactionUseCase
        private set
    lateinit var getMonthlySummaryUseCase: GetMonthlySummaryUseCase
        private set
    lateinit var getAccountsUseCase: GetAccountsUseCase
        private set
    lateinit var addAccountUseCase: AddAccountUseCase
        private set
    lateinit var updateAccountUseCase: UpdateAccountUseCase
        private set
    lateinit var deleteAccountUseCase: DeleteAccountUseCase
        private set
    lateinit var getCategoriesUseCase: GetCategoriesUseCase
        private set
    lateinit var addCategoryUseCase: AddCategoryUseCase
        private set
    lateinit var updateCategoryUseCase: UpdateCategoryUseCase
        private set
    lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
        private set
    lateinit var getBudgetsUseCase: GetBudgetsUseCase
        private set
    lateinit var addBudgetUseCase: AddBudgetUseCase
        private set
    lateinit var updateBudgetUseCase: UpdateBudgetUseCase
        private set
    lateinit var deleteBudgetUseCase: DeleteBudgetUseCase
        private set
    lateinit var getCategorySpendingUseCase: GetCategorySpendingUseCase
        private set
    lateinit var exportToCsvUseCase: ExportToCsvUseCase
        private set
    lateinit var getSyncAccountsUseCase: GetSyncAccountsUseCase
        private set
    lateinit var addSyncAccountUseCase: AddSyncAccountUseCase
        private set
    lateinit var deleteSyncAccountUseCase: DeleteSyncAccountUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDependencies()
    }

    private fun initDependencies() {
        database = Room.databaseBuilder(this, FinanceDatabase::class.java, FinanceDatabase.DATABASE_NAME)
            .addCallback(FinanceDatabase.seedCallback)
            .addMigrations(FinanceDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        accountRepository = AccountRepositoryImpl(database.accountDao())
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        budgetRepository = BudgetRepositoryImpl(database.budgetDao())
        syncAccountRepository = SyncAccountRepositoryImpl(database.syncAccountDao())

        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository, accountRepository, categoryRepository)
        addTransactionUseCase = AddTransactionUseCase(transactionRepository, accountRepository)
        updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository, accountRepository)
        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, accountRepository)
        getMonthlySummaryUseCase = GetMonthlySummaryUseCase(transactionRepository)
        getAccountsUseCase = GetAccountsUseCase(accountRepository)
        addAccountUseCase = AddAccountUseCase(accountRepository)
        updateAccountUseCase = UpdateAccountUseCase(accountRepository)
        deleteAccountUseCase = DeleteAccountUseCase(accountRepository)
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
        addCategoryUseCase = AddCategoryUseCase(categoryRepository)
        updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository)
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)
        getBudgetsUseCase = GetBudgetsUseCase(budgetRepository, categoryRepository)
        addBudgetUseCase = AddBudgetUseCase(budgetRepository)
        updateBudgetUseCase = UpdateBudgetUseCase(budgetRepository)
        deleteBudgetUseCase = DeleteBudgetUseCase(budgetRepository)
        getCategorySpendingUseCase = GetCategorySpendingUseCase(transactionRepository, categoryRepository)
        exportToCsvUseCase = ExportToCsvUseCase(transactionRepository, accountRepository, categoryRepository)
        getSyncAccountsUseCase = GetSyncAccountsUseCase(syncAccountRepository)
        addSyncAccountUseCase = AddSyncAccountUseCase(syncAccountRepository)
        deleteSyncAccountUseCase = DeleteSyncAccountUseCase(syncAccountRepository)
    }

    companion object {
        lateinit var instance: FinanceApplication
            private set

        fun get(context: Context) = context.applicationContext as FinanceApplication
    }
}
