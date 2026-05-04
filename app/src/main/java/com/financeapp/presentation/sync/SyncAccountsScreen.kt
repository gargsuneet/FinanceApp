package com.financeapp.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.FinanceApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncAccountsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncAccountViewModel = viewModel(factory = SyncAccountViewModel.Factory(FinanceApplication.instance))
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isSignedIn) "Cloud Sync" else "Sign In to Sync") },
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
        when {
            state.isSignedIn -> SyncedContent(state, viewModel, Modifier.padding(padding))
            state.mode == SyncScreenMode.CREATE_ACCOUNT -> CreateAccountContent(state, viewModel, Modifier.padding(padding))
            else -> SignInContent(state, viewModel, Modifier.padding(padding))
        }
    }

    if (state.showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::hideRestoreConfirm,
            icon = { Icon(Icons.Default.CloudDownload, null, tint = Color(0xFF4CAF50)) },
            title = { Text("Restore from Cloud?") },
            text = { Text("This will replace ALL your current data with the cloud backup. The app will restart. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = viewModel::downloadFromCloud,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Yes, Restore") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideRestoreConfirm) { Text("Cancel") }
            }
        )
    }

    if (state.showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::hideSignOutConfirm,
            title = { Text("Sign Out?") },
            text = { Text("Your local data won't be deleted. You can sign in again anytime.") },
            confirmButton = {
                Button(
                    onClick = viewModel::signOut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideSignOutConfirm) { Text("Cancel") }
            }
        )
    }

    if (state.showForgotPassword) {
        AlertDialog(
            onDismissRequest = viewModel::hideForgotPassword,
            icon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF2196F3)) },
            title = { Text("Reset Password") },
            text = {
                if (state.resetSent) {
                    Text("Password reset email sent to ${state.forgotPasswordEmail}. Check your inbox.", textAlign = TextAlign.Center)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("We'll send a reset link to:")
                        OutlinedTextField(
                            value = state.forgotPasswordEmail,
                            onValueChange = viewModel::setForgotEmail,
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                }
            },
            confirmButton = {
                if (!state.resetSent) {
                    Button(onClick = viewModel::sendPasswordReset) { Text("Send Reset Email") }
                } else {
                    Button(onClick = viewModel::hideForgotPassword) { Text("Done") }
                }
            },
            dismissButton = {
                if (!state.resetSent) TextButton(onClick = viewModel::hideForgotPassword) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SignInContent(state: SyncUiState, vm: SyncAccountViewModel, modifier: Modifier) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Surface(shape = CircleShape, color = Color(0xFF00897B).copy(alpha = 0.12f), modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Cloud, null, tint = Color(0xFF00897B), modifier = Modifier.size(44.dp))
            }
        }
        Text("Sync Your Data", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Sign in to sync your finance data across all your devices. Enter the same email on any device to access your data.",
            fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::setEmail,
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF00897B)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00897B)) },
                    trailingIcon = {
                        IconButton(onClick = vm::togglePasswordVisible) {
                            Icon(if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); vm.signIn() }),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                TextButton(onClick = vm::showForgotPassword, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot Password?", fontSize = 12.sp)
                }
                Button(
                    onClick = { focusManager.clearFocus(); vm.signIn() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", fontSize = 13.sp, color = Color(0xFF757575))
            TextButton(onClick = vm::switchToCreateAccount) {
                Text("Create Account", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CreateAccountContent(state: SyncUiState, vm: SyncAccountViewModel, modifier: Modifier) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Surface(shape = CircleShape, color = Color(0xFF00897B).copy(alpha = 0.12f), modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PersonAdd, null, tint = Color(0xFF00897B), modifier = Modifier.size(44.dp))
            }
        }
        Text("Create Sync Account", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Create a free account to back up and sync your data across devices.", fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = vm::setDisplayName,
                    label = { Text("Your Name (optional)") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF00897B)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true, shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::setEmail,
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF00897B)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    singleLine = true, shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    label = { Text("Password (min 6 chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00897B)) },
                    trailingIcon = {
                        IconButton(onClick = vm::togglePasswordVisible) {
                            Icon(if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); vm.createAccount() }),
                    singleLine = true, shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { focusManager.clearFocus(); vm.createAccount() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", fontSize = 13.sp, color = Color(0xFF757575))
            TextButton(onClick = vm::switchToSignIn) {
                Text("Sign In", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SyncedContent(state: SyncUiState, vm: SyncAccountViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF00897B), modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            state.userEmail.take(1).uppercase(),
                            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.userDisplayName.ifBlank { state.userEmail.substringBefore("@") }, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(state.userEmail, fontSize = 12.sp, color = Color(0xFF757575))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                        Text(" Signed in", fontSize = 11.sp, color = Color(0xFF4CAF50))
                    }
                }
                IconButton(onClick = vm::showSignOutConfirm) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color(0xFF9E9E9E))
                }
            }
        }

        state.lastSyncTime?.let { lastSync ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFF388E3C), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Last synced: $lastSync", fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Sync Data", fontSize = 12.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2196F3).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CloudUpload, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Push to Cloud", fontWeight = FontWeight.Medium)
                        Text("Upload your current data to cloud", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    if (state.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Button(
                            onClick = vm::uploadToCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text("Upload", fontSize = 13.sp) }
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF4CAF50).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CloudDownload, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pull from Cloud", fontWeight = FontWeight.Medium)
                        Text("Restore data from cloud backup", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    if (state.isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF4CAF50))
                    } else {
                        Button(
                            onClick = vm::showRestoreConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text("Restore", fontSize = 13.sp) }
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFF9800).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Sync, null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Sync", fontWeight = FontWeight.Medium)
                        Text(if (state.autoSyncEnabled) "Syncs every hour" else "Manual sync only", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    Switch(
                        checked = state.autoSyncEnabled,
                        onCheckedChange = { vm.toggleAutoSync() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF9800))
                    )
                }
            }
        }

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = Color(0xFFF9A825), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Use the same email on all your devices to sync data. " +
                    "Push uploads your current data. Pull replaces local data with cloud data.",
                    fontSize = 12.sp, color = Color(0xFF795548)
                )
            }
        }
    }
}
