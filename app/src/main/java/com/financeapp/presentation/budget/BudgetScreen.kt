package com.financeapp.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.Budget
import com.financeapp.presentation.components.BudgetProgressBar
import com.financeapp.presentation.components.categoryIconVector
import com.financeapp.presentation.components.formatAmount
import com.financeapp.presentation.components.parseColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(factory = BudgetViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    var showAddEditSheet by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) showAddEditSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.startAdd()
                showAddEditSheet = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Month selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().also {
                                it.set(state.year, state.month - 1, 1)
                                it.add(Calendar.MONTH, -1)
                            }
                            viewModel.setMonthYear(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                        val cal = Calendar.getInstance().also { it.set(state.year, state.month - 1, 1) }
                        Text(
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = {
                            val cal2 = Calendar.getInstance().also {
                                it.set(state.year, state.month - 1, 1)
                                it.add(Calendar.MONTH, 1)
                            }
                            viewModel.setMonthYear(cal2.get(Calendar.MONTH) + 1, cal2.get(Calendar.YEAR))
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Budget summary
            if (state.budgets.isNotEmpty()) {
                val totalBudget = state.budgets.sumOf { it.amount }
                val totalSpent = state.budgets.sumOf { it.spent }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00897B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Budget", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(formatAmount(totalSpent), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("/ ${formatAmount(totalBudget)}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            BudgetProgressBar(spent = totalSpent, budget = totalBudget, color = Color.White)
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.budgets.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00897B).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF00897B).copy(alpha = 0.5f)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("No budgets yet", color = Color(0xFF424242), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap + to set a budget\nfor a spending category",
                                color = Color(0xFF9E9E9E),
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(state.budgets) { budget ->
                    BudgetCard(
                        budget = budget,
                        onClick = {
                            viewModel.startEdit(budget)
                            showAddEditSheet = true
                        },
                        onDelete = { budgetToDelete = budget }
                    )
                }
            }
        }
    }

    if (showAddEditSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditSheet = false }) {
            AddEditBudgetContent(
                state = state,
                viewModel = viewModel,
                onDismiss = { showAddEditSheet = false }
            )
        }
    }

    budgetToDelete?.let { budget ->
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = { Text("Delete Budget") },
            text = { Text("Delete budget for '${budget.categoryName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(budget)
                    budgetToDelete = null
                }) { Text("Delete", color = Color(0xFFF44336)) }
            },
            dismissButton = { TextButton(onClick = { budgetToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun BudgetCard(budget: Budget, onClick: () -> Unit, onDelete: () -> Unit) {
    val color = parseColor(budget.categoryColor.ifBlank { "#9C27B0" })
    val progress = if (budget.amount > 0) (budget.spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIconVector(budget.categoryIcon), contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(budget.categoryName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        when {
                            progress >= 1f -> "Over budget!"
                            progress >= 0.8f -> "Near limit"
                            else -> "${String.format("%.1f", (1 - progress) * 100)}% remaining"
                        },
                        fontSize = 11.sp,
                        color = when {
                            progress >= 1f -> Color(0xFFF44336)
                            progress >= 0.8f -> Color(0xFFFF9800)
                            else -> Color(0xFF757575)
                        }
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            BudgetProgressBar(spent = budget.spent, budget = budget.amount, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetContent(
    state: BudgetUiState,
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit
) {
    val expenseCategories = state.categories.filter { it.type.name == "EXPENSE" || it.type.name == "BOTH" }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (state.editingBudget == null) "Add Budget" else "Edit Budget",
            fontWeight = FontWeight.Bold, fontSize = 18.sp
        )

        // Category dropdown
        var categoryExpanded by remember { mutableStateOf(false) }
        val selectedCategory = expenseCategories.find { it.id == state.editCategoryId }
        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                expenseCategories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name) },
                        onClick = { viewModel.setCategory(cat.id); categoryExpanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.editAmount,
            onValueChange = viewModel::setAmount,
            label = { Text("Budget Amount *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$  ") }
        )

        state.error?.let { Text(it, color = Color(0xFFF44336), fontSize = 12.sp) }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) { Text("Save") }
        }
    }
}
