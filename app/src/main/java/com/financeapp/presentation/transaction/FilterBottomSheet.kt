package com.financeapp.presentation.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    state: TransactionListUiState,
    onDismiss: () -> Unit,
    onSetTypeFilter: (TransactionType?) -> Unit,
    onSetDateFilter: (Long?, Long?) -> Unit,
    onSetAmountFilter: (Double?, Double?) -> Unit,
    onClearFilters: () -> Unit
) {
    var minAmountText by remember { mutableStateOf(state.minAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(state.maxAmount?.toString() ?: "") }
    var startDateText by remember { mutableStateOf(
        state.filterStartDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: ""
    )}
    var endDateText by remember { mutableStateOf(
        state.filterEndDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: ""
    )}

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Filter Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Text("Type", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val types = listOf<TransactionType?>(null, TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
                val labels = listOf("All", "Income", "Expense", "Transfer")
                types.forEachIndexed { i, type ->
                    FilterChip(
                        selected = state.selectedType == type,
                        onClick = { onSetTypeFilter(type) },
                        label = { Text(labels[i], fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Date Range", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("From (yyyy-MM-dd)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = { Text("To (yyyy-MM-dd)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("Amount Range", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minAmountText,
                    onValueChange = { minAmountText = it },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxAmountText,
                    onValueChange = { maxAmountText = it },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onClearFilters(); onDismiss() }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val start = try { sdf.parse(startDateText.trim())?.time } catch(e: Exception) { null }
                        val end = try { sdf.parse(endDateText.trim())?.time?.plus(86399999L) } catch(e: Exception) { null }
                        onSetDateFilter(start, end)
                        onSetAmountFilter(minAmountText.toDoubleOrNull(), maxAmountText.toDoubleOrNull())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
