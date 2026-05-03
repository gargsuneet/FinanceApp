package com.financeapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.MonthlySummary
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.usecase.GetMonthlySummaryUseCase
import com.financeapp.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0, month = 1, year = 2024),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dataJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        val month = state.selectedMonth
        val year = state.selectedYear
        val start = getMonthStart(month, year)
        val end = getMonthEnd(month, year)

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            combine(
                getMonthlySummaryUseCase(month, year),
                getTransactionsUseCase.byDateRange(start, end)
            ) { summary, transactions ->
                _uiState.update {
                    it.copy(
                        summary = summary,
                        recentTransactions = transactions,
                        totalBalance = summary.income - summary.expense,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun prevMonth() {
        val state = _uiState.value
        val cal = Calendar.getInstance()
        cal.set(state.selectedYear, state.selectedMonth - 1, 1)
        cal.add(Calendar.MONTH, -1)
        _uiState.update {
            it.copy(
                selectedMonth = cal.get(Calendar.MONTH) + 1,
                selectedYear = cal.get(Calendar.YEAR),
                isLoading = true
            )
        }
        loadData()
    }

    fun nextMonth() {
        val state = _uiState.value
        val cal = Calendar.getInstance()
        cal.set(state.selectedYear, state.selectedMonth - 1, 1)
        cal.add(Calendar.MONTH, 1)
        _uiState.update {
            it.copy(
                selectedMonth = cal.get(Calendar.MONTH) + 1,
                selectedYear = cal.get(Calendar.YEAR),
                isLoading = true
            )
        }
        loadData()
    }

    private fun getMonthStart(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getMonthEnd(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(year, month - 1, lastDay, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(app.getMonthlySummaryUseCase, app.getTransactionsUseCase) as T
    }
}
