package com.financeapp.domain.usecase

import com.financeapp.domain.model.*
import com.financeapp.domain.repository.AccountRepository
import com.financeapp.domain.repository.BudgetRepository
import com.financeapp.domain.repository.CategoryRepository
import com.financeapp.domain.repository.SyncAccountRepository
import com.financeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class GetTransactionsUseCase(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val categoryRepo: CategoryRepository
) {
    operator fun invoke(): Flow<List<Transaction>> =
        combine(
            transactionRepo.getAll(),
            accountRepo.getAll(),
            categoryRepo.getAll()
        ) { transactions, accounts, categories ->
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }
            transactions.map { t ->
                t.copy(
                    accountName = accountMap[t.accountId]?.name ?: "",
                    toAccountName = t.toAccountId?.let { accountMap[it]?.name } ?: "",
                    categoryName = t.categoryId?.let { categoryMap[it]?.name } ?: "",
                    categoryIcon = t.categoryId?.let { categoryMap[it]?.icon } ?: "",
                    categoryColor = t.categoryId?.let { categoryMap[it]?.color } ?: ""
                )
            }
        }

    fun byDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        combine(
            transactionRepo.getByDateRange(start, end),
            accountRepo.getAll(),
            categoryRepo.getAll()
        ) { transactions, accounts, categories ->
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }
            transactions.map { t ->
                t.copy(
                    accountName = accountMap[t.accountId]?.name ?: "",
                    toAccountName = t.toAccountId?.let { accountMap[it]?.name } ?: "",
                    categoryName = t.categoryId?.let { categoryMap[it]?.name } ?: "",
                    categoryIcon = t.categoryId?.let { categoryMap[it]?.icon } ?: "",
                    categoryColor = t.categoryId?.let { categoryMap[it]?.color } ?: ""
                )
            }
        }

    fun search(query: String): Flow<List<Transaction>> =
        combine(
            transactionRepo.search(query),
            accountRepo.getAll(),
            categoryRepo.getAll()
        ) { transactions, accounts, categories ->
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }
            transactions.map { t ->
                t.copy(
                    accountName = accountMap[t.accountId]?.name ?: "",
                    toAccountName = t.toAccountId?.let { accountMap[it]?.name } ?: "",
                    categoryName = t.categoryId?.let { categoryMap[it]?.name } ?: "",
                    categoryIcon = t.categoryId?.let { categoryMap[it]?.icon } ?: "",
                    categoryColor = t.categoryId?.let { categoryMap[it]?.color } ?: ""
                )
            }
        }
}

class AddTransactionUseCase (
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction): Long {
        val id = transactionRepo.insert(transaction)
        when (transaction.type) {
            TransactionType.INCOME -> accountRepo.addToBalance(transaction.accountId, transaction.amount)
            TransactionType.EXPENSE -> accountRepo.subtractFromBalance(transaction.accountId, transaction.amount + transaction.fee)
            TransactionType.TRANSFER -> {
                accountRepo.subtractFromBalance(transaction.accountId, transaction.amount + transaction.fee)
                transaction.toAccountId?.let { accountRepo.addToBalance(it, transaction.amount) }
            }
        }
        return id
    }
}

class UpdateTransactionUseCase (
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(old: Transaction, new: Transaction) {
        // Reverse old transaction effect
        when (old.type) {
            TransactionType.INCOME -> accountRepo.subtractFromBalance(old.accountId, old.amount)
            TransactionType.EXPENSE -> accountRepo.addToBalance(old.accountId, old.amount + old.fee)
            TransactionType.TRANSFER -> {
                accountRepo.addToBalance(old.accountId, old.amount + old.fee)
                old.toAccountId?.let { accountRepo.subtractFromBalance(it, old.amount) }
            }
        }
        // Apply new transaction effect
        when (new.type) {
            TransactionType.INCOME -> accountRepo.addToBalance(new.accountId, new.amount)
            TransactionType.EXPENSE -> accountRepo.subtractFromBalance(new.accountId, new.amount + new.fee)
            TransactionType.TRANSFER -> {
                accountRepo.subtractFromBalance(new.accountId, new.amount + new.fee)
                new.toAccountId?.let { accountRepo.addToBalance(it, new.amount) }
            }
        }
        transactionRepo.update(new)
    }
}

class DeleteTransactionUseCase (
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        // Reverse the transaction effect on account balance
        when (transaction.type) {
            TransactionType.INCOME -> accountRepo.subtractFromBalance(transaction.accountId, transaction.amount)
            TransactionType.EXPENSE -> accountRepo.addToBalance(transaction.accountId, transaction.amount + transaction.fee)
            TransactionType.TRANSFER -> {
                accountRepo.addToBalance(transaction.accountId, transaction.amount + transaction.fee)
                transaction.toAccountId?.let { accountRepo.subtractFromBalance(it, transaction.amount) }
            }
        }
        transactionRepo.deleteById(transaction.id)
    }
}

class GetAccountsUseCase (
    private val accountRepo: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> = accountRepo.getAll()
    fun totalBalance(): Flow<Double?> = accountRepo.getTotalBalance()
}

class AddAccountUseCase (private val accountRepo: AccountRepository) {
    suspend operator fun invoke(account: Account): Long = accountRepo.insert(account)
}

class UpdateAccountUseCase (private val accountRepo: AccountRepository) {
    suspend operator fun invoke(account: Account) = accountRepo.update(account)
}

class DeleteAccountUseCase (private val accountRepo: AccountRepository) {
    suspend operator fun invoke(account: Account) = accountRepo.delete(account)
}

class GetCategoriesUseCase (private val categoryRepo: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = categoryRepo.getAll()
    fun byType(type: String): Flow<List<Category>> = categoryRepo.getByType(type)
}

class AddCategoryUseCase (private val categoryRepo: CategoryRepository) {
    suspend operator fun invoke(category: Category): Long = categoryRepo.insert(category)
}

class UpdateCategoryUseCase (private val categoryRepo: CategoryRepository) {
    suspend operator fun invoke(category: Category) = categoryRepo.update(category)
}

class DeleteCategoryUseCase (private val categoryRepo: CategoryRepository) {
    suspend operator fun invoke(category: Category) = categoryRepo.delete(category)
}

class GetBudgetsUseCase (
    private val budgetRepo: BudgetRepository,
    private val categoryRepo: CategoryRepository
) {
    fun byMonthYear(month: Int, year: Int): Flow<List<Budget>> =
        combine(
            budgetRepo.getBudgetsWithSpending(month, year),
            categoryRepo.getAll()
        ) { budgets, categories ->
            val categoryMap = categories.associateBy { it.id }
            budgets.map { b ->
                b.copy(
                    categoryName = categoryMap[b.categoryId]?.name ?: "",
                    categoryIcon = categoryMap[b.categoryId]?.icon ?: "",
                    categoryColor = categoryMap[b.categoryId]?.color ?: ""
                )
            }
        }
}

class AddBudgetUseCase (private val budgetRepo: BudgetRepository) {
    suspend operator fun invoke(budget: Budget): Long = budgetRepo.insert(budget)
}

class UpdateBudgetUseCase (private val budgetRepo: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = budgetRepo.update(budget)
}

class DeleteBudgetUseCase (private val budgetRepo: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = budgetRepo.delete(budget)
}

class GetMonthlySummaryUseCase (
    private val transactionRepo: TransactionRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<MonthlySummary> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return combine(
            transactionRepo.getTotalByTypeAndDateRange("INCOME", start, end),
            transactionRepo.getTotalByTypeAndDateRange("EXPENSE", start, end)
        ) { income, expense ->
            MonthlySummary(
                income = income ?: 0.0,
                expense = expense ?: 0.0,
                month = month,
                year = year
            )
        }
    }
}

class GetCategorySpendingUseCase (
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<List<CategorySpending>> =
        combine(
            transactionRepo.getCategoryTotalsForDateRange(start, end),
            categoryRepo.getAll()
        ) { totals, categories ->
            val categoryMap = categories.associateBy { it.id }
            val totalAmount = totals.values.sum()
            totals.map { (catId, amount) ->
                val cat = catId?.let { categoryMap[it] }
                CategorySpending(
                    categoryId = catId,
                    categoryName = cat?.name ?: "Uncategorized",
                    categoryColor = cat?.color ?: "#607D8B",
                    categoryIcon = cat?.icon ?: "category",
                    amount = amount,
                    percentage = if (totalAmount > 0) (amount / totalAmount * 100).toFloat() else 0f
                )
            }.sortedByDescending { it.amount }
        }
}

class ExportToCsvUseCase (
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val categoryRepo: CategoryRepository
) {
    suspend operator fun invoke(): String {
        val accounts = accountRepo.getAll().first()
        val categories = categoryRepo.getAll().first()
        val transactions = transactionRepo.getAll().first()

        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }

        val sb = StringBuilder()
        sb.appendLine("Date,Type,Amount,Fee,Points,Account,To Account,Category,Note,Currency")
        transactions.forEach { t ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(t.date))
            val account = accountMap[t.accountId]?.name ?: ""
            val toAccount = t.toAccountId?.let { accountMap[it]?.name } ?: ""
            val category = t.categoryId?.let { categoryMap[it]?.name } ?: ""
            sb.appendLine("$date,${t.type},${t.amount},${t.fee},${t.points},$account,$toAccount,$category,\"${t.note}\",${t.currency}")
        }
        return sb.toString()
    }
}

class GetSyncAccountsUseCase(private val repo: SyncAccountRepository) {
    operator fun invoke(): Flow<List<SyncAccount>> = repo.getAll()
}
class AddSyncAccountUseCase(private val repo: SyncAccountRepository) {
    suspend operator fun invoke(syncAccount: SyncAccount): Long = repo.insert(syncAccount)
}
class DeleteSyncAccountUseCase(private val repo: SyncAccountRepository) {
    suspend operator fun invoke(syncAccount: SyncAccount) = repo.delete(syncAccount)
}
