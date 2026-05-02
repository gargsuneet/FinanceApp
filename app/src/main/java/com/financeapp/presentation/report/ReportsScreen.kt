package com.financeapp.presentation.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.financeapp.presentation.components.BarChart
import com.financeapp.presentation.components.PieChart
import com.financeapp.presentation.components.formatAmount
import com.financeapp.presentation.components.parseColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().also {
                                it.set(state.selectedYear, state.selectedMonth - 1, 1)
                                it.add(Calendar.MONTH, -1)
                            }
                            viewModel.setMonthYear(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
                        }) { Icon(Icons.Default.ChevronLeft, null) }

                        val cal = Calendar.getInstance().also {
                            it.set(state.selectedYear, state.selectedMonth - 1, 1)
                        }
                        Text(
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time),
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                        )

                        IconButton(onClick = {
                            val cal2 = Calendar.getInstance().also {
                                it.set(state.selectedYear, state.selectedMonth - 1, 1)
                                it.add(Calendar.MONTH, 1)
                            }
                            viewModel.setMonthYear(cal2.get(Calendar.MONTH) + 1, cal2.get(Calendar.YEAR))
                        }) { Icon(Icons.Default.ChevronRight, null) }
                    }
                }
            }

            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Income", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(formatAmount(state.currentSummary.income), color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Expense", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(formatAmount(state.currentSummary.expense), color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            val net = state.currentSummary.income - state.currentSummary.expense
                            Text(formatAmount(net), color = if (net >= 0) Color(0xFF81C784) else Color(0xFFEF9A9A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Tab: Pie Chart / Bar Chart
            item {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("By Category") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Trend") })
                }
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (selectedTab == 0) {
                // Pie chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Spending by Category", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Spacer(Modifier.height(12.dp))
                            if (state.categorySpending.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No expense data", color = Color(0xFF9E9E9E))
                                }
                            } else {
                                PieChart(data = state.categorySpending, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                // Category breakdown list
                if (state.categorySpending.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Spacer(Modifier.height(8.dp))
                                state.categorySpending.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Canvas(modifier = Modifier.size(10.dp)) {
                                                drawCircle(color = parseColor(item.categoryColor))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(item.categoryName, fontSize = 13.sp)
                                                Text("${String.format("%.1f", item.percentage)}%", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                            }
                                        }
                                        Text(formatAmount(item.amount), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFFF44336))
                                    }
                                    Divider(color = Color(0xFFF5F5F5))
                                }
                            }
                        }
                    }
                }
            } else {
                // Bar chart - trend
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (state.monthlySummaries.isEmpty()) {
                                Text("No trend data available", color = Color(0xFF9E9E9E))
                            } else {
                                BarChart(summaries = state.monthlySummaries, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                // Monthly breakdown table
                if (state.monthlySummaries.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Monthly Summary", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Spacer(Modifier.height(8.dp))
                                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                state.monthlySummaries.reversed().forEach { summary ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${months.getOrNull(summary.month - 1)} ${summary.year}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF424242),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(formatAmount(summary.income), fontSize = 12.sp, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                                        Text(formatAmount(summary.expense), fontSize = 12.sp, color = Color(0xFFF44336), modifier = Modifier.weight(1f))
                                    }
                                    Divider(color = Color(0xFFF5F5F5))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
