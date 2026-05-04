package com.financeapp.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.TransactionType
import com.financeapp.presentation.components.TransactionItem
import com.financeapp.presentation.components.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: TransactionListViewModel = viewModel(factory = TransactionListViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(
            state = state,
            onDismiss = { showFilterSheet = false },
            onSetTypeFilter = { viewModel.setTypeFilter(it) },
            onSetDateFilter = { start, end -> viewModel.setDateFilter(start, end) },
            onSetAmountFilter = { min, max -> viewModel.setAmountFilter(min, max) },
            onClearFilters = { viewModel.clearFilters() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Active filters chips
            if (state.selectedType != null || state.filterAccountId != null || state.filterCategoryId != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedType?.let { type ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text(type.name) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (state.filterAccountId != null || state.filterCategoryId != null) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilters() },
                            label = { Text("Clear Filters") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Type tabs
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(null to "All", TransactionType.INCOME to "Income",
                    TransactionType.EXPENSE to "Expense", TransactionType.TRANSFER to "Transfer")
                types.forEach { (type, label) ->
                    val isSelected = state.selectedType == type
                    Surface(
                        modifier = Modifier.clickable { viewModel.setTypeFilter(type) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        tonalElevation = if (isSelected) 0.dp else 1.dp
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Color(0xFF424242)
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF00897B).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF00897B).copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No transactions found",
                            color = Color(0xFF424242),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Try adjusting your filters\nor tap + to add a transaction",
                            color = Color(0xFF9E9E9E),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Group by date
                val grouped = state.transactions.groupBy {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.date))
                }
                val dailyIncome = state.transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val dailyExpense = state.transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                // Summary bar
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Income: ${formatAmount(dailyIncome)}", fontSize = 12.sp, color = Color(0xFF4CAF50))
                        Text("Expense: ${formatAmount(dailyExpense)}", fontSize = 12.sp, color = Color(0xFFF44336))
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    grouped.forEach { (date, transactions) ->
                        item {
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
