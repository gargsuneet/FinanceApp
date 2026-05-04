package com.financeapp

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.financeapp.data.local.dataStore
import com.financeapp.presentation.pin.PinScreen
import com.financeapp.presentation.pin.PinSetupScreen
import kotlinx.coroutines.flow.first
import com.financeapp.domain.model.TransactionType
import com.financeapp.presentation.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for previous crash
        val prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)
        if (lastCrash != null) {
            prefs.edit().remove("last_crash").apply()
            setContent {
                MaterialTheme {
                    CrashScreen(crashLog = lastCrash, onRetry = {
                        finish()
                        startActivity(intent)
                    })
                }
            }
            return
        }

        setContent {
            FinanceAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun CrashScreen(crashLog: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("⚠️ App Crashed", fontSize = 22.sp, color = Color.Red)
        Spacer(Modifier.height(8.dp))
        Text("Please share this error with support:", fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFFF5F5F5),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = crashLog,
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF212121)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Retry")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun FinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF00897B),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB2DFDB),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121)
        ),
        content = content
    )
}

private val NOTIFICATION_ASKED = booleanPreferencesKey("notification_permission_asked")

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var pinChecked by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("main") }

    // Request POST_NOTIFICATIONS permission once on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> /* user chose allow or deny — either is fine, app continues */ }

        LaunchedEffect(Unit) {
            val prefs = context.dataStore.data.first()
            val alreadyAsked = prefs[NOTIFICATION_ASKED] ?: false
            if (!alreadyAsked) {
                context.dataStore.edit { it[NOTIFICATION_ASKED] = true }
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val pinEnabled = prefs[booleanPreferencesKey("pin_enabled")] ?: false
        startDestination = if (pinEnabled) "pin_entry" else "main"
        pinChecked = true
    }

    if (!pinChecked) return

    NavHost(navController = navController, startDestination = startDestination) {
        composable("main") {
            MainScreen(
                onNavigateToAddTransaction = { type ->
                    navController.navigate("add_transaction/${type.name}")
                },
                onNavigateToEditTransaction = { id -> navController.navigate("edit_transaction/$id") },
                onNavigateToPinSetup = { navController.navigate("pin_setup") }
            )
        }
        composable(
            route = "add_transaction/{transactionType}",
            arguments = listOf(navArgument("transactionType") { type = NavType.StringType; defaultValue = "EXPENSE" })
        ) {
            com.financeapp.presentation.transaction.AddEditTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit_transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) {
            com.financeapp.presentation.transaction.AddEditTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("pin_setup") {
            PinSetupScreen(
                onPinSet = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("pin_entry") {
            PinScreen(
                onPinVerified = {
                    navController.navigate("main") {
                        popUpTo("pin_entry") { inclusive = true }
                    }
                }
            )
        }
    }
}
