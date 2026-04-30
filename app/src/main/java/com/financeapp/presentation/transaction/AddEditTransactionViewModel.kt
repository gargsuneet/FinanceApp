package com.financeapp.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.domain.model.*
import com.financeapp.domain.repository.TransactionRepository
import com.financeapp.domain.usecase.AddTransactionUseCase
import com.financeapp.domain.usecase.GetAccountsUseCase
import com.financeapp.domain.usecase.GetCategoriesUseCase
import com.financeapp.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

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
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private var originalTransaction: Transaction? = null

    init {
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
                getCategoriesUseCase()
            ) { accounts, categories ->
                _uiState.update { state ->
                    state.copy(
                        accounts = accounts,
                        categories = categories,
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
                        currency = transaction.currency
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
            currency = state.currency
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

    fun clearError() = _uiState.update { it.copy(error = null) }
}
