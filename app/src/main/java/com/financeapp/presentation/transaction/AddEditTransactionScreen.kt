package com.financeapp.presentation.transaction

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
import com.financeapp.domain.model.Category
import com.financeapp.domain.model.TransactionType
import com.financeapp.presentation.components.ACCOUNT_COLORS
import com.financeapp.presentation.components.CURRENCIES
import com.financeapp.presentation.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = viewModel(
        factory = AddEditTransactionViewModel.Factory(FinanceApplication.instance)
    )
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Calculator state
    var calcLeft by remember { mutableStateOf("") }
    var calcOp by remember { mutableStateOf<Char?>(null) }
    var calcRight by remember { mutableStateOf("") }

    // Sync existing amount (for edit mode)
    LaunchedEffect(state.amount) {
        if (calcLeft.isEmpty() && calcOp == null && state.amount.isNotEmpty() && state.amount != "0") {
            calcLeft = state.amount
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun evalAmount(): Double {
        val left = calcLeft.toDoubleOrNull() ?: 0.0
        return if (calcOp != null) {
            val right = calcRight.toDoubleOrNull() ?: 0.0
            if (calcOp == '+') left + right else left - right
        } else left
    }

    fun onCalcKey(key: String) {
        when (key) {
            "⌫" -> {
                if (calcOp != null) {
                    if (calcRight.isNotEmpty()) {
                        calcRight = calcRight.dropLast(1)
                    } else {
                        calcOp = null
                    }
                } else {
                    calcLeft = calcLeft.dropLast(1)
                }
            }
            "+", "-" -> {
                if (calcLeft.isNotEmpty()) {
                    // Evaluate any pending operation first
                    if (calcOp != null) {
                        val result = evalAmount()
                        calcLeft = if (result == result.toLong().toDouble() && result >= 0) {
                            result.toLong().toString()
                        } else {
                            String.format("%.2f", result)
                        }
                        calcRight = ""
                    }
                    calcOp = key[0]
                }
            }
            "." -> {
                if (calcOp != null) {
                    if (!calcRight.contains('.')) calcRight += "."
                } else {
                    if (!calcLeft.contains('.')) calcLeft += "."
                }
            }
            "00" -> {
                if (calcOp != null) {
                    if (calcRight.isNotEmpty() && calcRight != "0") calcRight += "00"
                } else {
                    if (calcLeft.isNotEmpty() && calcLeft != "0") calcLeft += "00"
                }
            }
            else -> {
                if (calcOp != null) {
                    if (calcRight == "0" && key != ".") calcRight = key
                    else if (calcRight.length < 12) calcRight += key
                } else {
                    if (calcLeft == "0" && key != ".") calcLeft = key
                    else if (calcLeft.length < 12) calcLeft += key
                }
            }
        }
    }

    fun handleSave() {
        val result = evalAmount()
        val amountStr = if (result == result.toLong().toDouble() && result > 0) {
            result.toLong().toString()
        } else {
            String.format("%.4f", result).trimEnd('0').trimEnd('.')
        }
        viewModel.setAmount(amountStr)
        viewModel.save()
    }

    val typeColor = when (state.type) {
        TransactionType.EXPENSE -> Color(0xFFF44336)
        TransactionType.INCOME -> Color(0xFF4CAF50)
        TransactionType.TRANSFER -> Color(0xFF2196F3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Type tabs in the top bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TransactionType.values().forEach { type ->
                            val isSelected = state.type == type
                            val tColor = when (type) {
                                TransactionType.EXPENSE -> Color(0xFFF44336)
                                TransactionType.INCOME -> Color(0xFF4CAF50)
                                TransactionType.TRANSFER -> Color(0xFF2196F3)
                            }
                            Surface(
                                modifier = Modifier
                                    .clickable { viewModel.setType(type) }
                                    .padding(horizontal = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) tColor else Color.Transparent
                            ) {
                                Text(
                                    type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (isSelected) Color.White else Color(0xFF757575),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF212121))
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            color = typeColor
                        )
                    } else {
                        TextButton(onClick = ::handleSave) {
                            Text("SAVE", fontWeight = FontWeight.Bold, color = typeColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Form fields (scrollable) ──────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp)
            ) {
                // Date row
                item {
                    FormRow(
                        label = "Date",
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        Text(
                            sdf.format(java.util.Date(state.date)),
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF212121)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Account row
                item {
                    AccountDropdownRow(
                        label = if (state.type == TransactionType.TRANSFER) "From" else "Account",
                        accounts = state.accounts,
                        selectedId = state.selectedAccountId,
                        onSelect = viewModel::setAccount,
                        onAddNew = { viewModel.showNewAccountForm(false) }
                    )
                }

                // To Account row (Transfer only)
                if (state.type == TransactionType.TRANSFER) {
                    item {
                        AccountDropdownRow(
                            label = "To",
                            accounts = state.accounts.filter { it.id != state.selectedAccountId },
                            selectedId = state.toAccountId,
                            onSelect = { viewModel.setToAccount(it) },
                            onAddNew = { viewModel.showNewAccountForm(true) }
                        )
                    }
                }

                // Category row (not for Transfer)
                if (state.type != TransactionType.TRANSFER) {
                    item {
                        CategoryDropdownRow(
                            categories = state.categories.filter {
                                it.type.name == state.type.name || it.type.name == "BOTH"
                            },
                            allCategories = state.categories,
                            selectedId = state.selectedCategoryId,
                            onSelect = viewModel::setCategory
                        )
                    }
                }

                // Note row
                item {
                    var noteExpanded by remember { mutableStateOf(false) }
                    FormRow(label = "Note") {
                        if (noteExpanded) {
                            OutlinedTextField(
                                value = state.note,
                                onValueChange = viewModel::setNote,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter note...", color = Color(0xFFBDBDBD)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                )
                            )
                        } else {
                            Text(
                                state.note.ifEmpty { "Enter note..." },
                                color = if (state.note.isEmpty()) Color(0xFFBDBDBD) else Color(0xFF212121),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { noteExpanded = true }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Sync Account row
                item {
                    FormRow(label = "Sync") {
                        var syncExpanded by remember { mutableStateOf(false) }
                        val selectedSyncName = state.syncAccounts.find { it.id == state.syncAccountId }?.name ?: "None"
                        ExposedDropdownMenuBox(
                            expanded = syncExpanded,
                            onExpandedChange = { syncExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedSyncName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = syncExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                            )
                            ExposedDropdownMenu(
                                expanded = syncExpanded,
                                onDismissRequest = { syncExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = { viewModel.setSyncAccountId(null); syncExpanded = false }
                                )
                                state.syncAccounts.forEach { sa ->
                                    DropdownMenuItem(
                                        text = { Text(sa.name) },
                                        onClick = { viewModel.setSyncAccountId(sa.id); syncExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // Fee row
                item {
                    FormRow(label = "Fee") {
                        OutlinedTextField(
                            value = state.fee,
                            onValueChange = viewModel::setFee,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0.00", color = Color(0xFFBDBDBD)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                    }
                }

                // Points row
                item {
                    FormRow(label = "Points") {
                        OutlinedTextField(
                            value = state.points,
                            onValueChange = viewModel::setPoints,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0", color = Color(0xFFBDBDBD)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                    }
                }
            }

            // ── Amount display ─────────────────────────────────────────────
            Divider(color = Color(0xFFEEEEEE))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (calcOp != null) {
                        Text(
                            "${calcLeft} ${calcOp} ${calcRight.ifEmpty { "" }}",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    Text(
                        String.format("%.2f", evalAmount()),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            // ── Calculator keypad ──────────────────────────────────────────
            Divider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val keyRows = listOf(
                    listOf("7", "8", "9", "⌫"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "-"),
                    listOf(".", "0", "00", "✓")
                )
                keyRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        row.forEach { key ->
                            val isAction = key in listOf("+", "-", "⌫", "✓")
                            val isSave = key == "✓"
                            val keyBg = when {
                                isSave -> typeColor
                                isAction -> Color(0xFFE0E0E0)
                                else -> Color.White
                            }
                            val keyTextColor = when {
                                isSave -> Color.White
                                key == "+" -> Color(0xFF4CAF50)
                                key == "-" -> Color(0xFFF44336)
                                key == "⌫" -> Color(0xFF757575)
                                else -> Color(0xFF212121)
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable {
                                        if (isSave) handleSave() else onCalcKey(key)
                                    },
                                shape = RoundedCornerShape(4.dp),
                                color = keyBg,
                                shadowElevation = if (isAction || key == "0") 0.dp else 1.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = if (isSave) 22.sp else 18.sp,
                                        fontWeight = if (isAction) FontWeight.Bold else FontWeight.Normal,
                                        color = keyTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Date picker dialog
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

    // New account bottom sheet
    if (state.showNewAccountSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::hideNewAccountForm) {
            NewAccountContent(state = state, viewModel = viewModel)
        }
    }
}

// ── Form row helper ──────────────────────────────────────────────────────────

@Composable
private fun FormRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                modifier = Modifier.width(80.dp),
                fontSize = 13.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                content = content
            )
        }
        Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

// ── Account dropdown row ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdownRow(
    label: String,
    accounts: List<Account>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedId }

    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                modifier = Modifier.width(80.dp),
                fontSize = 13.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedAccount?.name ?: "Select account",
                        fontWeight = FontWeight.Medium,
                        color = if (selectedAccount == null) Color(0xFFBDBDBD) else Color(0xFF212121),
                        fontSize = 14.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF9E9E9E))
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(parseColor(account.color).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(account.name.take(1), fontSize = 10.sp, color = parseColor(account.color))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(account.name, fontSize = 13.sp)
                                        Text(account.type.name, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                    }
                                }
                            },
                            onClick = {
                                onSelect(account.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = onAddNew,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("New", fontSize = 12.sp)
            }
        }
        Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

// ── Category dropdown row ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdownRow(
    categories: List<Category>,
    allCategories: List<Category>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedId }

    val categoryLabel: (Category) -> String = { cat ->
        if (cat.parentId != null) {
            val parent = allCategories.find { it.id == cat.parentId }
            "${parent?.name ?: ""} > ${cat.name}"
        } else {
            cat.name
        }
    }

    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Category",
                modifier = Modifier.width(80.dp),
                fontSize = 13.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedCategory != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(selectedCategory.color).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    selectedCategory.name.take(1),
                                    fontSize = 9.sp,
                                    color = parseColor(selectedCategory.color)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                categoryLabel(selectedCategory),
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF212121),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text("Select category", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF9E9E9E))
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Group: parents first, then children indented
                    val parents = categories.filter { it.parentId == null }
                    val childrenByParent = categories.filter { it.parentId != null }.groupBy { it.parentId }
                    val standalone = categories.filter { it.parentId != null && parents.none { p -> p.id == it.parentId } }

                    parents.forEach { parent ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(parseColor(parent.color).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(parent.name.take(1), fontSize = 10.sp, color = parseColor(parent.color))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(parent.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            },
                            onClick = { onSelect(parent.id); expanded = false }
                        )
                        childrenByParent[parent.id]?.forEach { child ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(parseColor(child.color).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(child.name.take(1), fontSize = 9.sp, color = parseColor(child.color))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(child.name, fontSize = 13.sp, color = Color(0xFF424242))
                                    }
                                },
                                onClick = { onSelect(child.id); expanded = false }
                            )
                        }
                    }
                    // Standalone children (whose parent doesn't match current type filter)
                    standalone.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(categoryLabel(cat), fontSize = 13.sp) },
                            onClick = { onSelect(cat.id); expanded = false }
                        )
                    }
                }
            }
        }
        Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

// ── New account bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewAccountContent(
    state: AddEditTransactionUiState,
    viewModel: AddEditTransactionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "New Account",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        OutlinedTextField(
            value = state.newAccountName,
            onValueChange = viewModel::setNewAccountName,
            label = { Text("Account Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Account Type", fontSize = 12.sp, color = Color(0xFF757575))
        val types = AccountType.values()
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(types.take(3), types.drop(3)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { type ->
                        FilterChip(
                            selected = state.newAccountType == type,
                            onClick = { viewModel.setNewAccountType(type) },
                            label = { Text(type.name.replace('_', ' '), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Pad if odd number
                    if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.newAccountBalance,
                onValueChange = viewModel::setNewAccountBalance,
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
                    value = state.newAccountCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    CURRENCIES.take(12).forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                viewModel.setNewAccountCurrency(currency)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Text("Color", fontSize = 12.sp, color = Color(0xFF757575))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ACCOUNT_COLORS.take(9).forEach { colorHex ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                        .clickable { viewModel.setNewAccountColor(colorHex) }
                        .then(
                            if (state.newAccountColor == colorHex)
                                Modifier.border(2.5.dp, Color.Black, CircleShape)
                            else Modifier
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = viewModel::hideNewAccountForm,
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }
            Button(
                onClick = viewModel::saveNewAccount,
                modifier = Modifier.weight(1f)
            ) { Text("Save") }
        }
    }
}
