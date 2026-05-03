package com.financeapp.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.SyncAccount
import com.financeapp.domain.usecase.AddSyncAccountUseCase
import com.financeapp.domain.usecase.DeleteSyncAccountUseCase
import com.financeapp.domain.usecase.GetSyncAccountsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SyncAccountUiState(
    val syncAccounts: List<SyncAccount> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newEmail: String = "",
    val newColor: String = "#2196F3"
)

class SyncAccountViewModel(
    private val getSyncAccountsUseCase: GetSyncAccountsUseCase,
    private val addSyncAccountUseCase: AddSyncAccountUseCase,
    private val deleteSyncAccountUseCase: DeleteSyncAccountUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SyncAccountUiState())
    val uiState: StateFlow<SyncAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSyncAccountsUseCase().collect { list ->
                _uiState.update { it.copy(syncAccounts = list, isLoading = false) }
            }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false, newName = "", newEmail = "", newColor = "#2196F3") }
    fun setNewName(v: String) = _uiState.update { it.copy(newName = v) }
    fun setNewEmail(v: String) = _uiState.update { it.copy(newEmail = v) }
    fun setNewColor(v: String) = _uiState.update { it.copy(newColor = v) }

    fun addSyncAccount() {
        val state = _uiState.value
        if (state.newEmail.isBlank() || state.newName.isBlank()) return
        viewModelScope.launch {
            addSyncAccountUseCase(SyncAccount(email = state.newEmail, name = state.newName, color = state.newColor))
            hideAddDialog()
        }
    }

    fun deleteSyncAccount(sa: SyncAccount) {
        viewModelScope.launch { deleteSyncAccountUseCase(sa) }
    }

    companion object {
        fun Factory(app: FinanceApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SyncAccountViewModel(app.getSyncAccountsUseCase, app.addSyncAccountUseCase, app.deleteSyncAccountUseCase) as T
        }
    }
}
