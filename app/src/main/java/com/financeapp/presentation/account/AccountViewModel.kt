package com.financeapp.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.domain.model.Account
import com.financeapp.domain.model.AccountType
import com.financeapp.domain.usecase.AddAccountUseCase
import com.financeapp.domain.usecase.DeleteAccountUseCase
import com.financeapp.domain.usecase.GetAccountsUseCase
import com.financeapp.domain.usecase.UpdateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    // Edit state
    val editingAccount: Account? = null,
    val editName: String = "",
    val editType: AccountType = AccountType.CASH,
    val editBalance: String = "",
    val editCurrency: String = "USD",
    val editColor: String = "#2196F3",
    val editCreditLimit: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getAccountsUseCase(),
                getAccountsUseCase.totalBalance()
            ) { accounts, total ->
                _uiState.update { it.copy(accounts = accounts, totalBalance = total ?: 0.0, isLoading = false) }
            }.collect()
        }
    }

    fun startAddAccount() {
        _uiState.update {
            it.copy(
                editingAccount = null,
                editName = "",
                editType = AccountType.CASH,
                editBalance = "",
                editCurrency = "USD",
                editColor = "#2196F3",
                editCreditLimit = "",
                isSaved = false
            )
        }
    }

    fun startEditAccount(account: Account) {
        _uiState.update {
            it.copy(
                editingAccount = account,
                editName = account.name,
                editType = account.type,
                editBalance = account.balance.toString(),
                editCurrency = account.currency,
                editColor = account.color,
                editCreditLimit = if (account.creditLimit > 0) account.creditLimit.toString() else "",
                isSaved = false
            )
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(editName = name) }
    fun setType(type: AccountType) = _uiState.update { it.copy(editType = type) }
    fun setBalance(balance: String) = _uiState.update { it.copy(editBalance = balance) }
    fun setCurrency(currency: String) = _uiState.update { it.copy(editCurrency = currency) }
    fun setColor(color: String) = _uiState.update { it.copy(editColor = color) }
    fun setCreditLimit(limit: String) = _uiState.update { it.copy(editCreditLimit = limit) }

    fun saveAccount() {
        val state = _uiState.value
        if (state.editName.isBlank()) {
            _uiState.update { it.copy(error = "Account name is required") }
            return
        }
        val balance = state.editBalance.toDoubleOrNull() ?: 0.0
        val creditLimit = state.editCreditLimit.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val account = Account(
                id = state.editingAccount?.id ?: 0L,
                name = state.editName,
                type = state.editType,
                balance = balance,
                currency = state.editCurrency,
                color = state.editColor,
                icon = accountTypeToIcon(state.editType),
                creditLimit = creditLimit
            )
            if (state.editingAccount == null) {
                addAccountUseCase(account)
            } else {
                updateAccountUseCase(account)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch { deleteAccountUseCase(account) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun accountTypeToIcon(type: AccountType) = when (type) {
        AccountType.CASH -> "payments"
        AccountType.BANK -> "account_balance"
        AccountType.CREDIT_CARD -> "credit_card"
        AccountType.SAVINGS -> "savings"
        AccountType.INVESTMENT -> "trending_up"
        AccountType.OTHER -> "account_balance_wallet"
    }
}
