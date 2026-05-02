package com.financeapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.MonthlySummary
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.usecase.GetMonthlySummaryUseCase
import com.financeapp.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0, month = 1, year = 2024),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        viewModelScope.launch {
            combine(
                getMonthlySummaryUseCase(month, year),
                getTransactionsUseCase()
            ) { summary, transactions ->
                HomeUiState(
                    summary = summary,
                    recentTransactions = transactions.take(10),
                    totalBalance = summary.income - summary.expense,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(app.getMonthlySummaryUseCase, app.getTransactionsUseCase) as T
    }
}
