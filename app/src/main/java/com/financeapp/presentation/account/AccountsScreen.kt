package com.financeapp.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.Account
import com.financeapp.domain.model.AccountType
import com.financeapp.presentation.components.ACCOUNT_COLORS
import com.financeapp.presentation.components.CURRENCIES
import com.financeapp.presentation.components.categoryIconVector
import com.financeapp.presentation.components.formatAmount
import com.financeapp.presentation.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    var showAddEditSheet by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) showAddEditSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.startAddAccount()
                showAddEditSheet = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Account", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00897B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(
                            formatAmount(state.totalBalance),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(state.accounts) { account ->
                AccountCard(
                    account = account,
                    onClick = {
                        viewModel.startEditAccount(account)
                        showAddEditSheet = true
                    },
                    onDelete = { accountToDelete = account }
                )
            }
        }
    }

    if (showAddEditSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditSheet = false }) {
            AddEditAccountContent(
                state = state,
                viewModel = viewModel,
                onDismiss = { showAddEditSheet = false }
            )
        }
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete '${account.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account)
                    accountToDelete = null
                }) { Text("Delete", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AccountCard(account: Account, onClick: () -> Unit, onDelete: () -> Unit) {
    val color = parseColor(account.color)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIconVector(account.icon), contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "${account.type.name} • ${account.currency}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                if (account.creditLimit > 0) {
                    Text("Limit: ${formatAmount(account.creditLimit, account.currency)}", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatAmount(account.balance, account.currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (account.balance >= 0) Color(0xFF212121) else Color(0xFFF44336)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountContent(
    state: AccountUiState,
    viewModel: AccountViewModel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (state.editingAccount == null) "Add Account" else "Edit Account",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        OutlinedTextField(
            value = state.editName,
            onValueChange = viewModel::setName,
            label = { Text("Account Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Account Type", fontSize = 12.sp, color = Color(0xFF757575))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AccountType.values().take(3).forEach { type ->
                val isSelected = state.editType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setType(type) },
                    label = { Text(type.name, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AccountType.values().drop(3).forEach { type ->
                val isSelected = state.editType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setType(type) },
                    label = { Text(type.name, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.editBalance,
                onValueChange = viewModel::setBalance,
                label = { Text("Initial Balance") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            var currencyExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = state.editCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                    CURRENCIES.take(10).forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = { viewModel.setCurrency(currency); currencyExpanded = false }
                        )
                    }
                }
            }
        }

        if (state.editType == AccountType.CREDIT_CARD) {
            OutlinedTextField(
                value = state.editCreditLimit,
                onValueChange = viewModel::setCreditLimit,
                label = { Text("Credit Limit") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Column {
            Text("Color", fontSize = 12.sp, color = Color(0xFF757575))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ACCOUNT_COLORS.take(8).forEach { colorHex ->
                    val color = parseColor(colorHex)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { viewModel.setColor(colorHex) }
                            .then(if (state.editColor == colorHex) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = viewModel::saveAccount, modifier = Modifier.weight(1f)) { Text("Save") }
        }

        state.error?.let { error ->
            Text(error, color = Color(0xFFF44336), fontSize = 12.sp)
        }
    }
}
