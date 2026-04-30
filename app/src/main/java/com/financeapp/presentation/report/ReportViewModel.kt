package com.financeapp.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.domain.model.CategorySpending
import com.financeapp.domain.model.MonthlySummary
import com.financeapp.domain.usecase.GetCategorySpendingUseCase
import com.financeapp.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReportUiState(
    val categorySpending: List<CategorySpending> = emptyList(),
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val currentSummary: MonthlySummary = MonthlySummary(0.0, 0.0, month = 1, year = 2024),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReports()
        loadLast6Months()
    }

    private fun loadReports() {
        val state = _uiState.value
        val cal = Calendar.getInstance()
        cal.set(state.selectedYear, state.selectedMonth - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(state.selectedYear, state.selectedMonth - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val end = cal.timeInMillis

        viewModelScope.launch {
            combine(
                getCategorySpendingUseCase(start, end),
                getMonthlySummaryUseCase(state.selectedMonth, state.selectedYear)
            ) { spending, summary ->
                _uiState.update {
                    it.copy(categorySpending = spending, currentSummary = summary, isLoading = false)
                }
            }.collect()
        }
    }

    private fun loadLast6Months() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val summaries = mutableListOf<MonthlySummary>()
            repeat(6) {
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                getMonthlySummaryUseCase(month, year).firstOrNull()?.let { summaries.add(it) }
                cal.add(Calendar.MONTH, -1)
            }
            _uiState.update { it.copy(monthlySummaries = summaries.reversed()) }
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year, isLoading = true) }
        loadReports()
    }
}
