package com.financeapp.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.model.TransactionType
import com.financeapp.domain.usecase.DeleteTransactionUseCase
import com.financeapp.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedType: TransactionType? = null,
    val filterStartDate: Long? = null,
    val filterEndDate: Long? = null,
    val filterAccountId: Long? = null,
    val filterCategoryId: Long? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            searchQuery.debounce(300).flatMapLatest { query ->
                if (query.isBlank()) getTransactionsUseCase()
                else getTransactionsUseCase.search(query)
            }.collect { transactions ->
                val filtered = applyFilters(transactions)
                _uiState.update { it.copy(transactions = filtered, isLoading = false) }
            }
        }
    }

    private fun applyFilters(transactions: List<Transaction>): List<Transaction> {
        var result = transactions
        val state = _uiState.value
        state.selectedType?.let { type -> result = result.filter { it.type == type } }
        state.filterAccountId?.let { accId -> result = result.filter { it.accountId == accId || it.toAccountId == accId } }
        state.filterCategoryId?.let { catId -> result = result.filter { it.categoryId == catId } }
        state.filterStartDate?.let { start -> result = result.filter { it.date >= start } }
        state.filterEndDate?.let { end -> result = result.filter { it.date <= end } }
        return result
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _uiState.update { it.copy(selectedType = type) }
        loadTransactions()
    }

    fun setDateFilter(start: Long?, end: Long?) {
        _uiState.update { it.copy(filterStartDate = start, filterEndDate = end) }
        loadTransactions()
    }

    fun setAccountFilter(accountId: Long?) {
        _uiState.update { it.copy(filterAccountId = accountId) }
        loadTransactions()
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.update { it.copy(filterCategoryId = categoryId) }
        loadTransactions()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedType = null,
                filterStartDate = null,
                filterEndDate = null,
                filterAccountId = null,
                filterCategoryId = null
            )
        }
        loadTransactions()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}
