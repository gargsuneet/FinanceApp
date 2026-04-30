package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.financeapp.presentation.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
fun FinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFBBDEFB),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121)
        ),
        content = content
    )
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToAddTransaction = { navController.navigate("add_transaction") },
                onNavigateToEditTransaction = { id -> navController.navigate("edit_transaction/$id") }
            )
        }
        composable("add_transaction") {
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
    }
}
