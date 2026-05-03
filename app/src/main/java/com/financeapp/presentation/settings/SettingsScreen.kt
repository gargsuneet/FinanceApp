package com.financeapp.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSyncAccountsClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importCsv(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.exportShareUri.collect { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share CSV"))
        }
    }

    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.importMessage) {
        state.importMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            item {
                Text("General", fontSize = 12.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.AttachMoney,
                            iconColor = Color(0xFF4CAF50),
                            title = "Default Currency",
                            subtitle = state.defaultCurrency,
                            onClick = {}
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.Notifications,
                            iconColor = Color(0xFFFF9800),
                            title = "Notifications",
                            subtitle = "Budget alerts & reminders",
                            onClick = {}
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            iconColor = Color(0xFF607D8B),
                            title = "PIN Lock",
                            subtitle = if (state.isPinEnabled) "Enabled" else "Disabled",
                            onClick = viewModel::togglePin
                        )
                    }
                }
            }

            item {
                Text("Sync", fontSize = 12.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    SettingsRow(
                        icon = Icons.Default.People,
                        iconColor = Color(0xFF2196F3),
                        title = "Sync Accounts",
                        subtitle = "Share book with others",
                        onClick = onSyncAccountsClick
                    )
                }
            }

            item {
                Text("Data", fontSize = 12.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.FileDownload,
                            iconColor = Color(0xFF2196F3),
                            title = "Export to CSV",
                            subtitle = "Share transactions as CSV",
                            onClick = viewModel::exportToCsv,
                            isLoading = state.isExporting
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.FileUpload,
                            iconColor = Color(0xFF4CAF50),
                            title = "Import from CSV",
                            subtitle = "Import transactions from CSV file",
                            onClick = { importLauncher.launch("text/*") }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.Backup,
                            iconColor = Color(0xFF9C27B0),
                            title = "Backup Data",
                            subtitle = "Save data to local storage",
                            onClick = {}
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.RestoreFromTrash,
                            iconColor = Color(0xFFF44336),
                            title = "Restore Data",
                            subtitle = "Restore from backup",
                            onClick = {}
                        )
                    }
                }
            }

            item {
                Text("About", fontSize = 12.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            iconColor = Color(0xFF607D8B),
                            title = "Version",
                            subtitle = "1.0.0",
                            onClick = {}
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            icon = Icons.Default.PrivacyTip,
                            iconColor = Color(0xFF607D8B),
                            title = "Privacy Policy",
                            subtitle = "View privacy policy",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF757575))
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}
