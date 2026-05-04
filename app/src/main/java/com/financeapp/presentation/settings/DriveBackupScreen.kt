package com.financeapp.presentation.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication
import com.financeapp.data.remote.GoogleDriveService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveBackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.onGoogleSignInSuccess(account)
            } catch (e: Exception) {
                viewModel.setDriveMessage("Sign-in failed: ${e.message}")
            }
        }
    }

    LaunchedEffect(state.driveMessage) {
        state.driveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDriveMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Drive Backup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Account Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Google Account",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.driveSignedIn) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF4285F4).copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4285F4),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(state.driveAccountName, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text(state.driveAccountEmail, fontSize = 12.sp, color = Color(0xFF757575))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.signOutGoogle(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    } else {
                        Text(
                            "Sign in with Google to back up and restore your data across devices.",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
                                    .requestServerAuthCode(GoogleDriveService.GOOGLE_WEB_CLIENT_ID)
                                    .build()
                                signInLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in with Google")
                        }
                    }
                }
            }

            if (state.driveSignedIn) {
                // Last backup info
                state.lastDriveBackupTime?.let { lastBackup ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Last backup", fontSize = 12.sp, color = Color(0xFF388E3C))
                                Text(lastBackup, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                // Backup / Restore / Auto-backup actions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        DriveActionRow(
                            icon = Icons.Default.CloudUpload,
                            iconColor = Color(0xFF4285F4),
                            title = "Backup to Google Drive",
                            subtitle = "Upload current data to your Drive",
                            isLoading = state.isDriveUploading,
                            onClick = { viewModel.backupToDrive(context) }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        DriveActionRow(
                            icon = Icons.Default.CloudDownload,
                            iconColor = Color(0xFF34A853),
                            title = "Restore from Google Drive",
                            subtitle = "Download and replace current data",
                            isLoading = state.isDriveDownloading,
                            onClick = { viewModel.restoreFromDrive(context) }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        DriveActionRow(
                            icon = Icons.Default.Autorenew,
                            iconColor = Color(0xFFFF9800),
                            title = "Auto Backup",
                            subtitle = if (state.driveAutoBackupEnabled) "Daily backup enabled" else "Tap to enable daily auto-backup",
                            isLoading = false,
                            onClick = { viewModel.toggleDriveAutoBackup(context) }
                        )
                    }
                }

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF9A825),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Backups are stored in a 'FinanceApp_Backup' folder in your Google Drive. " +
                            "Restoring will replace ALL current data and restart the app.",
                            fontSize = 12.sp,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
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
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = iconColor)
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}
