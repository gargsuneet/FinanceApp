package com.financeapp.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.Budget
import com.financeapp.domain.model.Category
import com.financeapp.domain.usecase.AddBudgetUseCase
import com.financeapp.domain.usecase.DeleteBudgetUseCase
import com.financeapp.domain.usecase.GetBudgetsUseCase
import com.financeapp.domain.usecase.GetCategoriesUseCase
import com.financeapp.domain.usecase.UpdateBudgetUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = true,
    val editingBudget: Budget? = null,
    val editCategoryId: Long? = null,
    val editAmount: String = "",
    val editCurrency: String = "USD",
    val isSaved: Boolean = false,
    val error: String? = null
)

class BudgetViewModel(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        viewModelScope.launch {
            combine(
                getBudgetsUseCase.byMonthYear(state.month, state.year),
                getCategoriesUseCase()
            ) { budgets, categories ->
                _uiState.update { it.copy(budgets = budgets, categories = categories, isLoading = false) }
            }.collect()
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _uiState.update { it.copy(month = month, year = year, isLoading = true) }
        loadData()
    }

    fun startAdd() {
        _uiState.update { it.copy(editingBudget = null, editCategoryId = null, editAmount = "", editCurrency = "USD", isSaved = false) }
    }

    fun startEdit(budget: Budget) {
        _uiState.update {
            it.copy(editingBudget = budget, editCategoryId = budget.categoryId, editAmount = budget.amount.toString(), editCurrency = budget.currency, isSaved = false)
        }
    }

    fun setCategory(categoryId: Long) = _uiState.update { it.copy(editCategoryId = categoryId) }
    fun setAmount(amount: String) = _uiState.update { it.copy(editAmount = amount) }
    fun setCurrency(currency: String) = _uiState.update { it.copy(editCurrency = currency) }

    fun save() {
        val state = _uiState.value
        val amount = state.editAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (state.editCategoryId == null) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }
        viewModelScope.launch {
            val budget = Budget(
                id = state.editingBudget?.id ?: 0L,
                categoryId = state.editCategoryId,
                amount = amount,
                month = state.month,
                year = state.year,
                currency = state.editCurrency
            )
            if (state.editingBudget == null) addBudgetUseCase(budget)
            else updateBudgetUseCase(budget)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete(budget: Budget) {
        viewModelScope.launch { deleteBudgetUseCase(budget) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BudgetViewModel(
                app.getBudgetsUseCase,
                app.addBudgetUseCase,
                app.updateBudgetUseCase,
                app.deleteBudgetUseCase,
                app.getCategoriesUseCase
            ) as T
    }
}
