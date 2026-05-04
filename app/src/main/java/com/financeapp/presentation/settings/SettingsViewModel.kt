package com.financeapp.presentation.settings

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.data.export.XlsxWriter
import com.financeapp.data.local.dataStore
import com.financeapp.data.remote.CurrencyApiService
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.model.TransactionType
import com.financeapp.domain.usecase.AddTransactionUseCase
import com.financeapp.domain.usecase.ExportToCsvUseCase
import com.financeapp.domain.usecase.GetAccountsUseCase
import com.financeapp.domain.usecase.GetCategoriesUseCase
import com.financeapp.domain.usecase.GetTransactionsUseCase
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
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val defaultCurrency: String = "USD",
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val backupMessage: String? = null,
    val isExporting: Boolean = false,
    val isPinEnabled: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val budgetAlertsEnabled: Boolean = false,
    val exchangeRates: Map<String, Double> = emptyMap()
)

class SettingsViewModel(
    private val exportToCsvUseCase: ExportToCsvUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val app: FinanceApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _exportShareUri = MutableSharedFlow<Uri>()
    val exportShareUri: SharedFlow<Uri> = _exportShareUri.asSharedFlow()

    private val _backupShareUri = MutableSharedFlow<Uri>()
    val backupShareUri: SharedFlow<Uri> = _backupShareUri.asSharedFlow()

    private val _excelShareUri = MutableSharedFlow<Uri>()
    val excelShareUri: SharedFlow<Uri> = _excelShareUri.asSharedFlow()

    val availableCurrencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "MXN",
        "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "AED")

    companion object {
        private val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
    }

    init {
        viewModelScope.launch {
            app.applicationContext.dataStore.data.collect { prefs ->
                _uiState.update { it.copy(
                    isPinEnabled = prefs[PIN_ENABLED] ?: false,
                    notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: false,
                    dailyReminderEnabled = prefs[DAILY_REMINDER_ENABLED] ?: false,
                    budgetAlertsEnabled = prefs[BUDGET_ALERTS_ENABLED] ?: false
                )}
            }
        }
    }

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

    fun exportToExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val transactions = getTransactionsUseCase().first()
                val bytes = XlsxWriter.createXlsx(transactions)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "finance_export_$timestamp.xlsx"
                val dir = app.getExternalFilesDir(null) ?: app.filesDir
                val file = File(dir, fileName)
                file.writeBytes(bytes)
                val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                _excelShareUri.emit(uri)
                _uiState.update { it.copy(exportMessage = "Excel ready to share", isExporting = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Excel export failed: ${e.message}", isExporting = false) }
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
                                            type = type, amount = amount, fee = fee, points = points,
                                            accountId = account.id, toAccountId = toAccount?.id,
                                            categoryId = category?.id, note = note, date = date, currency = currency
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

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = app.getDatabasePath(com.financeapp.data.local.FinanceDatabase.DATABASE_NAME)
                app.contentResolver.openOutputStream(uri)?.use { out ->
                    dbFile.inputStream().copyTo(out)
                }
                _uiState.update { it.copy(backupMessage = "Backup saved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Backup failed: ${e.message}") }
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = app.getDatabasePath(com.financeapp.data.local.FinanceDatabase.DATABASE_NAME)
                app.database.close()
                app.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    android.os.Process.killProcess(android.os.Process.myPid())
                }, 500)
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Restore failed: ${e.message}") }
            }
        }
    }

    fun fetchAndSaveRates() {
        viewModelScope.launch {
            val rates = CurrencyApiService.fetchRates(_uiState.value.defaultCurrency)
            rates?.let { _uiState.update { s -> s.copy(exchangeRates = it) } }
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

    fun clearMessage() = _uiState.update { it.copy(exportMessage = null, importMessage = null, backupMessage = null) }

    fun setCurrency(currency: String) {
        _uiState.update { it.copy(defaultCurrency = currency) }
        fetchAndSaveRates()
    }

    fun setupPin(pin: String) {
        val hash = hashPin(pin)
        viewModelScope.launch {
            app.applicationContext.dataStore.edit { prefs ->
                prefs[PIN_ENABLED] = true
                prefs[PIN_HASH] = hash
            }
        }
        _uiState.update { it.copy(isPinEnabled = true) }
    }

    fun disablePin() {
        viewModelScope.launch {
            app.applicationContext.dataStore.edit { prefs ->
                prefs[PIN_ENABLED] = false
                prefs.remove(PIN_HASH)
            }
        }
        _uiState.update { it.copy(isPinEnabled = false) }
    }

    fun verifyPin(pin: String): Boolean {
        val hash = hashPin(pin)
        val storedHash = kotlinx.coroutines.runBlocking {
            app.applicationContext.dataStore.data.first()[PIN_HASH] ?: ""
        }
        return hash == storedHash
    }

    fun togglePin() {
        if (_uiState.value.isPinEnabled) disablePin()
    }

    private fun hashPin(pin: String): String {
        return MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.applicationContext.dataStore.edit { prefs -> prefs[NOTIFICATIONS_ENABLED] = enabled }
        }
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.applicationContext.dataStore.edit { prefs -> prefs[DAILY_REMINDER_ENABLED] = enabled }
        }
        _uiState.update { it.copy(dailyReminderEnabled = enabled) }
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.applicationContext.dataStore.edit { prefs -> prefs[BUDGET_ALERTS_ENABLED] = enabled }
        }
        _uiState.update { it.copy(budgetAlertsEnabled = enabled) }
    }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                app.exportToCsvUseCase, app.getAccountsUseCase, app.getCategoriesUseCase,
                app.addTransactionUseCase, app.getTransactionsUseCase, app
            ) as T
    }
}
