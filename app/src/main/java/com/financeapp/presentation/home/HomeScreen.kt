package com.financeapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.presentation.components.SummaryCard
import com.financeapp.presentation.components.TransactionItem
import com.financeapp.presentation.components.formatAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Good Day!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                }

                item {
                    SummaryCard(
                        income = state.summary.income,
                        expense = state.summary.expense,
                        balance = state.summary.income - state.summary.expense
                    )
                }

                item {
                    QuickStatsRow(
                        income = state.summary.income,
                        expense = state.summary.expense
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Transactions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(
                            "See All",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable(onClick = onSeeAllClick)
                        )
                    }
                }

                if (state.recentTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet.\nTap + to add one!", color = Color(0xFF9E9E9E), fontSize = 14.sp)
                        }
                    }
                } else {
                    items(state.recentTransactions) { transaction ->
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

@Composable
fun QuickStatsRow(income: Double, expense: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Net Savings",
            value = formatAmount(income - expense),
            valueColor = if (income >= expense) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Savings Rate",
            value = if (income > 0) "${String.format("%.1f", (income - expense) / income * 100)}%" else "N/A",
            valueColor = Color(0xFF1976D2),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF757575))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
