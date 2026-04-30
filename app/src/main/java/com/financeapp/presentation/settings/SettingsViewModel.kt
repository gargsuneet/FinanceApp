package com.financeapp.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.domain.usecase.ExportToCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val defaultCurrency: String = "USD",
    val exportMessage: String? = null,
    val isExporting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportToCsvUseCase: ExportToCsvUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val csv = exportToCsvUseCase()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "finance_export_$timestamp.csv"
                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                val file = File(dir, fileName)
                file.writeText(csv)
                _uiState.update { it.copy(exportMessage = "Exported to ${file.absolutePath}", isExporting = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}", isExporting = false) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(exportMessage = null) }
    fun setCurrency(currency: String) = _uiState.update { it.copy(defaultCurrency = currency) }
}
