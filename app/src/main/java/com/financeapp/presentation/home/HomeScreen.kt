package com.financeapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.TransactionType
import com.financeapp.presentation.components.TransactionItem
import com.financeapp.presentation.components.formatAmount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()

    val monthLabel = remember(state.selectedMonth, state.selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(state.selectedYear, state.selectedMonth - 1, 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Month selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::prevMonth) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = Color(0xFF00897B))
                    }
                    Text(
                        text = monthLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF212121)
                    )
                    IconButton(onClick = viewModel::nextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = Color(0xFF00897B))
                    }
                }
                Divider(color = Color(0xFFEEEEEE))
            }

            // AndroMoney-style summary bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Income", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        Text(
                            formatAmount(state.summary.income),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Balance", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        Text(
                            formatAmount(state.totalBalance),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00897B)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Expense", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        Text(
                            formatAmount(state.summary.expense),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF44336)
                        )
                    }
                }
                Divider(color = Color(0xFFEEEEEE))
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else if (state.recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
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
                                "No transactions yet",
                                color = Color(0xFF424242),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap + to record your first transaction",
                                color = Color(0xFF9E9E9E),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                val grouped = state.recentTransactions.groupBy {
                    java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(java.util.Date(it.date))
                }
                grouped.entries.sortedByDescending { it.key }.forEach { (dateKey, txns) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF00897B).copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cal = Calendar.getInstance()
                            cal.time = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateKey)!!
                            val dayStr = java.text.SimpleDateFormat("EEEE dd", Locale.getDefault()).format(cal.time)
                            val monthStr = java.text.SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                            Text(dayStr, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF00897B))
                            Text(monthStr, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            val dayTotal = txns.sumOf {
                                when (it.type) {
                                    com.financeapp.domain.model.TransactionType.EXPENSE -> -it.amount
                                    com.financeapp.domain.model.TransactionType.INCOME -> it.amount
                                    else -> 0.0
                                }
                            }
                            Text(
                                String.format("%+.2f", dayTotal),
                                fontSize = 13.sp,
                                color = if (dayTotal >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    items(txns) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                        Divider(
                            modifier = Modifier.padding(start = 70.dp),
                            color = Color(0xFFF5F5F5),
                            thickness = 0.5.dp
                        )
                    }
                }
                item {
                    TextButton(
                        onClick = onSeeAllClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("See all transactions", color = Color(0xFF00897B))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onAddTransaction(TransactionType.EXPENSE) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = Color(0xFFFF6F00),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}
