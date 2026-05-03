package com.financeapp.presentation.sync

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.SyncAccount
import com.financeapp.presentation.components.parseColor

val SYNC_COLORS = listOf("#2196F3", "#F44336", "#4CAF50", "#FF9800", "#9C27B0", "#00BCD4", "#795548", "#607D8B")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncAccountsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncAccountViewModel = viewModel(factory = SyncAccountViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Accounts / Shared Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddDialog,
                containerColor = Color(0xFFFF6F00),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Email")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Add email addresses to share transactions. Tag transactions with a sync account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (state.syncAccounts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, modifier = Modifier.size(48.dp), tint = Color(0xFFBDBDBD))
                            Spacer(Modifier.height(8.dp))
                            Text("No sync accounts yet", color = Color(0xFF9E9E9E))
                            Text("Tap + to add one", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(state.syncAccounts) { sa ->
                    SyncAccountItem(sa = sa, onDelete = { viewModel.deleteSyncAccount(sa) })
                }
            }
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideAddDialog,
            title = { Text("Add Sync Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.newName,
                        onValueChange = viewModel::setNewName,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.newEmail,
                        onValueChange = viewModel::setNewEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SYNC_COLORS.forEach { colorHex ->
                            val selected = state.newColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 36.dp else 32.dp)
                                    .background(parseColor(colorHex), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { viewModel.setNewColor(colorHex) }, modifier = Modifier.size(32.dp)) {
                                    if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addSyncAccount) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideAddDialog) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SyncAccountItem(sa: SyncAccount, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(parseColor(sa.color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    sa.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sa.name, fontWeight = FontWeight.Medium)
                    if (sa.isOwner) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                        Text(" You", fontSize = 11.sp, color = Color(0xFFFF9800))
                    }
                }
                Text(sa.email, fontSize = 12.sp, color = Color(0xFF757575))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFF44336))
            }
        }
    }
}
