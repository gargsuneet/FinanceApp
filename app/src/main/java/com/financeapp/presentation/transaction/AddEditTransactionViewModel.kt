package com.financeapp.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.*
import com.financeapp.domain.model.CategoryType
import com.financeapp.domain.repository.TransactionRepository
import com.financeapp.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditTransactionUiState(
    val id: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val fee: String = "",
    val points: String = "",
    val selectedAccountId: Long? = null,
    val toAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val currency: String = "USD",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val syncAccounts: List<SyncAccount> = emptyList(),
    val syncAccountId: Long? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Inline new account creation
    val showNewAccountSheet: Boolean = false,
    val newAccountIsToAccount: Boolean = false,
    val newAccountName: String = "",
    val newAccountType: AccountType = AccountType.CASH,
    val newAccountBalance: String = "0",
    val newAccountCurrency: String = "USD",
    val newAccountColor: String = "#2196F3",
    // Inline category creation
    val newCategoryName: String = "",
    val newCategoryColor: String = "#9C27B0",
    val newCategoryParentId: Long? = null,
    // Photo attachment
    val photoUri: String? = null,
    val recurringNextDate: Long? = null
)

class AddEditTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSyncAccountsUseCase: GetSyncAccountsUseCase,
    private val transactionRepository: TransactionRepository,
    private val addAccountUseCase: AddAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private var originalTransaction: Transaction? = null

    init {
        val typeArg = savedStateHandle.get<String>("transactionType")
        val initialType = typeArg?.let {
            runCatching { TransactionType.valueOf(it) }.getOrNull()
        } ?: TransactionType.EXPENSE
        _uiState.update { it.copy(type = initialType) }

        loadAccountsAndCategories()

        val transactionId = savedStateHandle.get<Long>("transactionId") ?: 0L
        if (transactionId > 0) {
            loadTransaction(transactionId)
        }
    }

    private fun loadAccountsAndCategories() {
        viewModelScope.launch {
            combine(
                getAccountsUseCase(),
                getCategoriesUseCase(),
                getSyncAccountsUseCase()
            ) { accounts, categories, syncAccounts ->
                _uiState.update { state ->
                    state.copy(
                        accounts = accounts,
                        categories = categories,
                        syncAccounts = syncAccounts,
                        selectedAccountId = state.selectedAccountId ?: accounts.firstOrNull()?.id
                    )
                }
            }.collect()
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.getById(id).firstOrNull()?.let { transaction ->
                originalTransaction = transaction
                _uiState.update {
                    it.copy(
                        id = transaction.id,
                        type = transaction.type,
                        amount = transaction.amount.toString(),
                        fee = if (transaction.fee > 0) transaction.fee.toString() else "",
                        points = if (transaction.points > 0) transaction.points.toString() else "",
                        selectedAccountId = transaction.accountId,
                        toAccountId = transaction.toAccountId,
                        selectedCategoryId = transaction.categoryId,
                        note = transaction.note,
                        date = transaction.date,
                        isRecurring = transaction.isRecurring,
                        recurringPeriod = transaction.recurringPeriod,
                        currency = transaction.currency,
                        syncAccountId = transaction.syncAccountId,
                        photoUri = transaction.photoUri
                    )
                }
            }
        }
    }

    fun setType(type: TransactionType) = _uiState.update { it.copy(type = type, selectedCategoryId = null) }
    fun setAmount(amount: String) = _uiState.update { it.copy(amount = amount) }
    fun setFee(fee: String) = _uiState.update { it.copy(fee = fee) }
    fun setPoints(points: String) = _uiState.update { it.copy(points = points) }
    fun setAccount(accountId: Long) = _uiState.update { it.copy(selectedAccountId = accountId) }
    fun setToAccount(accountId: Long?) = _uiState.update { it.copy(toAccountId = accountId) }
    fun setCategory(categoryId: Long?) = _uiState.update { it.copy(selectedCategoryId = categoryId) }
    fun setNote(note: String) = _uiState.update { it.copy(note = note) }
    fun setDate(date: Long) = _uiState.update { it.copy(date = date) }
    fun setRecurring(isRecurring: Boolean) = _uiState.update { it.copy(isRecurring = isRecurring) }
    fun setRecurringPeriod(period: RecurringPeriod?) = _uiState.update { it.copy(recurringPeriod = period) }
    fun setCurrency(currency: String) = _uiState.update { it.copy(currency = currency) }
    fun setSyncAccountId(id: Long?) = _uiState.update { it.copy(syncAccountId = id) }

    // Inline account creation
    fun showNewAccountForm(isToAccount: Boolean) {
        _uiState.update {
            it.copy(
                showNewAccountSheet = true,
                newAccountIsToAccount = isToAccount,
                newAccountName = "",
                newAccountType = AccountType.CASH,
                newAccountBalance = "0",
                newAccountCurrency = it.currency,
                newAccountColor = "#2196F3"
            )
        }
    }

    fun hideNewAccountForm() = _uiState.update { it.copy(showNewAccountSheet = false) }
    fun setNewAccountName(name: String) = _uiState.update { it.copy(newAccountName = name) }
    fun setNewAccountType(type: AccountType) = _uiState.update { it.copy(newAccountType = type) }
    fun setNewAccountBalance(balance: String) = _uiState.update { it.copy(newAccountBalance = balance) }
    fun setNewAccountCurrency(currency: String) = _uiState.update { it.copy(newAccountCurrency = currency) }
    fun setNewAccountColor(color: String) = _uiState.update { it.copy(newAccountColor = color) }

    fun saveNewAccount() {
        val state = _uiState.value
        if (state.newAccountName.isBlank()) {
            _uiState.update { it.copy(error = "Account name is required") }
            return
        }
        viewModelScope.launch {
            val account = Account(
                id = 0L,
                name = state.newAccountName,
                type = state.newAccountType,
                balance = state.newAccountBalance.toDoubleOrNull() ?: 0.0,
                currency = state.newAccountCurrency,
                color = state.newAccountColor,
                icon = accountTypeToIcon(state.newAccountType)
            )
            val newId = addAccountUseCase(account)
            _uiState.update {
                if (it.newAccountIsToAccount) {
                    it.copy(toAccountId = newId, showNewAccountSheet = false)
                } else {
                    it.copy(selectedAccountId = newId, showNewAccountSheet = false)
                }
            }
        }
    }

    private fun accountTypeToIcon(type: AccountType) = when (type) {
        AccountType.CASH -> "payments"
        AccountType.BANK -> "account_balance"
        AccountType.CREDIT_CARD -> "credit_card"
        AccountType.SAVINGS -> "savings"
        AccountType.INVESTMENT -> "trending_up"
        AccountType.OTHER -> "account_balance_wallet"
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (state.selectedAccountId == null) {
            _uiState.update { it.copy(error = "Please select an account") }
            return
        }
        if (state.type == TransactionType.TRANSFER && state.toAccountId == null) {
            _uiState.update { it.copy(error = "Please select a destination account") }
            return
        }

        val recurringNextDate = if (state.isRecurring) {
            val cal = java.util.Calendar.getInstance()
            when (state.recurringPeriod) {
                RecurringPeriod.DAILY -> { cal.add(java.util.Calendar.DAY_OF_YEAR, 1); cal.timeInMillis }
                RecurringPeriod.WEEKLY -> { cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); cal.timeInMillis }
                RecurringPeriod.MONTHLY -> { cal.add(java.util.Calendar.MONTH, 1); cal.timeInMillis }
                RecurringPeriod.YEARLY -> { cal.add(java.util.Calendar.YEAR, 1); cal.timeInMillis }
                null -> { cal.add(java.util.Calendar.MONTH, 1); cal.timeInMillis }
            }
        } else null

        val transaction = Transaction(
            id = state.id,
            type = state.type,
            amount = amount,
            fee = state.fee.toDoubleOrNull() ?: 0.0,
            points = state.points.toDoubleOrNull() ?: 0.0,
            accountId = state.selectedAccountId,
            toAccountId = state.toAccountId,
            categoryId = state.selectedCategoryId,
            note = state.note,
            date = state.date,
            isRecurring = state.isRecurring,
            recurringPeriod = state.recurringPeriod,
            currency = state.currency,
            syncAccountId = state.syncAccountId,
            photoUri = state.photoUri,
            recurringNextDate = recurringNextDate
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (state.id == 0L) {
                    addTransactionUseCase(transaction)
                } else {
                    originalTransaction?.let { orig ->
                        updateTransactionUseCase(orig, transaction)
                    }
                }
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun setNewCategoryName(name: String) = _uiState.update { it.copy(newCategoryName = name) }
    fun setNewCategoryColor(color: String) = _uiState.update { it.copy(newCategoryColor = color) }
    fun setNewCategoryParentId(id: Long?) = _uiState.update { it.copy(newCategoryParentId = id) }
    fun setPhotoUri(uri: String?) = _uiState.update { it.copy(photoUri = uri) }

    fun saveNewCategory(name: String, type: CategoryType, color: String) {
        viewModelScope.launch {
            val app = FinanceApplication.instance
            val newId = app.categoryRepository.insert(
                com.financeapp.domain.model.Category(name = name, type = type, color = color, parentId = null)
            )
            val updated = app.categoryRepository.getAll().first()
            _uiState.update { it.copy(categories = updated, selectedCategoryId = newId) }
        }
    }

    fun saveNewSubCategory(parentId: Long, name: String, color: String) {
        viewModelScope.launch {
            val app = FinanceApplication.instance
            val parentCat = app.categoryRepository.getAll().first().find { it.id == parentId }
            val catType = parentCat?.type ?: if (_uiState.value.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            val newId = app.categoryRepository.insert(
                com.financeapp.domain.model.Category(name = name, type = catType, color = color, parentId = parentId)
            )
            val updated = app.categoryRepository.getAll().first()
            _uiState.update { it.copy(categories = updated, selectedCategoryId = newId) }
        }
    }

    fun saveNewSyncAccount(name: String, email: String, color: String) {
        viewModelScope.launch {
            val app = FinanceApplication.instance
            val newId = app.syncAccountRepository.insert(
                com.financeapp.domain.model.SyncAccount(name = name, email = email, color = color, isOwner = false)
            )
            val updated = app.syncAccountRepository.getAll().first()
            _uiState.update { it.copy(syncAccounts = updated, syncAccountId = newId) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val savedStateHandle = extras.createSavedStateHandle()
            return AddEditTransactionViewModel(
                app.addTransactionUseCase,
                app.updateTransactionUseCase,
                app.getAccountsUseCase,
                app.getCategoriesUseCase,
                app.getSyncAccountsUseCase,
                app.transactionRepository,
                app.addAccountUseCase,
                savedStateHandle
            ) as T
        }
    }
}
