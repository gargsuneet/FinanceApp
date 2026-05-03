package com.financeapp.presentation.settings

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.model.TransactionType
import com.financeapp.domain.usecase.AddTransactionUseCase
import com.financeapp.domain.usecase.ExportToCsvUseCase
import com.financeapp.domain.usecase.GetAccountsUseCase
import com.financeapp.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val defaultCurrency: String = "USD",
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val isExporting: Boolean = false,
    val isPinEnabled: Boolean = false
)

class SettingsViewModel(
    private val exportToCsvUseCase: ExportToCsvUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val app: FinanceApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _exportShareUri = MutableSharedFlow<Uri>()
    val exportShareUri: SharedFlow<Uri> = _exportShareUri.asSharedFlow()

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val csv = exportToCsvUseCase()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "finance_export_$timestamp.csv"
                val dir = app.getExternalFilesDir(null) ?: app.filesDir
                val file = File(dir, fileName)
                file.writeText(csv)
                val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                _exportShareUri.emit(uri)
                _uiState.update { it.copy(exportMessage = "Ready to share", isExporting = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}", isExporting = false) }
            }
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val accounts = getAccountsUseCase().first()
                val categories = getCategoriesUseCase().first()
                val accountMap = accounts.associateBy { it.name }
                val categoryMap = categories.associateBy { it.name }
                var imported = 0
                var failed = 0
                app.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).useLines { lines ->
                        lines.drop(1).forEach { line ->
                            try {
                                val cols = parseCsvLine(line)
                                if (cols.size >= 5) {
                                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(cols[0].trim())?.time
                                        ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cols[0].trim())?.time
                                        ?: System.currentTimeMillis()
                                    val type = TransactionType.valueOf(cols[1].trim().uppercase())
                                    val amount = cols[2].trim().toDoubleOrNull() ?: 0.0
                                    val fee = cols[3].trim().toDoubleOrNull() ?: 0.0
                                    val points = cols[4].trim().toDoubleOrNull() ?: 0.0
                                    val accountName = if (cols.size > 5) cols[5].trim() else ""
                                    val toAccountName = if (cols.size > 6) cols[6].trim() else ""
                                    val categoryName = if (cols.size > 7) cols[7].trim() else ""
                                    val note = if (cols.size > 8) cols[8].trim().removeSurrounding("\"") else ""
                                    val currency = if (cols.size > 9) cols[9].trim().ifBlank { "USD" } else "USD"
                                    val account = accountMap[accountName] ?: accounts.firstOrNull()
                                    val category = categoryMap[categoryName]
                                    val toAccount = accountMap[toAccountName]
                                    if (account != null) {
                                        addTransactionUseCase(Transaction(
                                            type = type,
                                            amount = amount,
                                            fee = fee,
                                            points = points,
                                            accountId = account.id,
                                            toAccountId = toAccount?.id,
                                            categoryId = category?.id,
                                            note = note,
                                            date = date,
                                            currency = currency
                                        ))
                                        imported++
                                    } else failed++
                                } else failed++
                            } catch (e: Exception) { failed++ }
                        }
                    }
                }
                _uiState.update { it.copy(importMessage = "Imported $imported transactions${if (failed > 0) ", $failed failed" else ""}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(importMessage = "Import failed: ${e.message}") }
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }

    fun clearMessage() = _uiState.update { it.copy(exportMessage = null, importMessage = null) }
    fun setCurrency(currency: String) = _uiState.update { it.copy(defaultCurrency = currency) }
    fun togglePin() = _uiState.update { it.copy(isPinEnabled = !it.isPinEnabled) }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(app.exportToCsvUseCase, app.getAccountsUseCase, app.getCategoriesUseCase, app.addTransactionUseCase, app) as T
    }
}
