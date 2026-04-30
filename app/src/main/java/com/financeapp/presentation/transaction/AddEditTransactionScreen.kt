package com.financeapp.presentation.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.domain.model.RecurringPeriod
import com.financeapp.domain.model.TransactionType
import com.financeapp.presentation.components.CURRENCIES
import com.financeapp.presentation.components.categoryIconVector
import com.financeapp.presentation.components.formatFullDate
import com.financeapp.presentation.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == 0L) "Add Transaction" else "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 8.dp))
                    } else {
                        TextButton(onClick = viewModel::save) {
                            Text("SAVE", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transaction Type Selector
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TransactionType.values().forEach { type ->
                            val isSelected = state.type == type
                            val color = when (type) {
                                TransactionType.INCOME -> Color(0xFF4CAF50)
                                TransactionType.EXPENSE -> Color(0xFFF44336)
                                TransactionType.TRANSFER -> Color(0xFF2196F3)
                            }
                            Surface(
                                modifier = Modifier.weight(1f).padding(4.dp)
                                    .clickable { viewModel.setType(type) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) color else Color.Transparent
                            ) {
                                Text(
                                    type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                    color = if (isSelected) Color.White else color,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Amount Field
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Amount *", fontSize = 12.sp, color = Color(0xFF757575))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = state.amount,
                                onValueChange = viewModel::setAmount,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                placeholder = { Text("0.00") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier.clickable { showCurrencyDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Text(state.currency, modifier = Modifier.padding(8.dp), color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Fee & Points Fields
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Additional Details", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF607D8B), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = state.fee,
                                onValueChange = viewModel::setFee,
                                label = { Text("Bank Fee / Service Charge") },
                                placeholder = { Text("0.00") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF607D8B),
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                ),
                                prefix = { Text("$  ", color = Color(0xFF607D8B)) }
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = state.points,
                                onValueChange = viewModel::setPoints,
                                label = { Text("Loyalty Points Earned / Redeemed") },
                                placeholder = { Text("0") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9800),
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                ),
                                prefix = { Text("★  ", color = Color(0xFFFF9800)) }
                            )
                        }
                    }
                }
            }

            // Account Selector
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (state.type == TransactionType.TRANSFER) "From Account *" else "Account *",
                            fontSize = 12.sp, color = Color(0xFF757575)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.accounts) { account ->
                                val isSelected = state.selectedAccountId == account.id
                                val acColor = parseColor(account.color)
                                Surface(
                                    modifier = Modifier.clickable { viewModel.setAccount(account.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) acColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                                    border = if (isSelected) BorderStroke(1.5.dp, acColor) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(account.name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                        Text(account.type.name, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // To Account (for Transfer)
            if (state.type == TransactionType.TRANSFER) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("To Account *", fontSize = 12.sp, color = Color(0xFF757575))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(state.accounts.filter { it.id != state.selectedAccountId }) { account ->
                                    val isSelected = state.toAccountId == account.id
                                    val acColor = parseColor(account.color)
                                    Surface(
                                        modifier = Modifier.clickable { viewModel.setToAccount(account.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) acColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                                        border = if (isSelected) BorderStroke(1.5.dp, acColor) else null
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(account.name, fontSize = 13.sp)
                                            Text(account.type.name, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Selector
            if (state.type != TransactionType.TRANSFER) {
                item {
                    val filteredCategories = state.categories.filter {
                        it.type.name == state.type.name || it.type.name == "BOTH"
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Category", fontSize = 12.sp, color = Color(0xFF757575))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(filteredCategories) { category ->
                                    val isSelected = state.selectedCategoryId == category.id
                                    val catColor = parseColor(category.color)
                                    Column(
                                        modifier = Modifier.clickable { viewModel.setCategory(category.id) },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f))
                                                .then(if (isSelected) Modifier.border(2.dp, catColor, CircleShape) else Modifier),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = categoryIconVector(category.icon),
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else catColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            category.name,
                                            fontSize = 10.sp,
                                            color = if (isSelected) catColor else Color(0xFF757575),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date Picker
            item {
                Card(
                    modifier = Modifier.clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF757575))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Date", fontSize = 12.sp, color = Color(0xFF757575))
                            Text(formatFullDate(state.date), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Note
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        maxLines = 3,
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Recurring
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = Color(0xFF757575))
                                Spacer(Modifier.width(8.dp))
                                Text("Recurring Transaction")
                            }
                            Switch(checked = state.isRecurring, onCheckedChange = viewModel::setRecurring)
                        }
                        if (state.isRecurring) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RecurringPeriod.values().forEach { period ->
                                    val isSelected = state.recurringPeriod == period
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setRecurringPeriod(period) },
                                        label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                LazyColumn {
                    items(CURRENCIES) { currency ->
                        Text(
                            currency,
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.setCurrency(currency)
                                showCurrencyDialog = false
                            }.padding(12.dp),
                            color = if (state.currency == currency) MaterialTheme.colorScheme.primary else Color(0xFF212121)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}
