package com.financeapp.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeapp.presentation.account.AccountsScreen
import com.financeapp.presentation.budget.BudgetScreen
import com.financeapp.presentation.category.CategoriesScreen
import com.financeapp.presentation.home.HomeScreen
import com.financeapp.presentation.report.ReportsScreen
import com.financeapp.presentation.settings.SettingsScreen
import com.financeapp.presentation.transaction.TransactionListScreen

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Transactions : BottomNavItem("transactions", "Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Reports : BottomNavItem("reports", "Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Budget : BottomNavItem("budget", "Budget", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    object More : BottomNavItem("more", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Transactions,
    BottomNavItem.Reports,
    BottomNavItem.Budget,
    BottomNavItem.More
)

@Composable
fun MainScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onAddTransaction = onNavigateToAddTransaction,
                    onTransactionClick = onNavigateToEditTransaction,
                    onSeeAllClick = {
                        navController.navigate(BottomNavItem.Transactions.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Transactions.route) {
                TransactionListScreen(
                    onAddTransaction = onNavigateToAddTransaction,
                    onTransactionClick = onNavigateToEditTransaction
                )
            }
            composable(BottomNavItem.Reports.route) {
                ReportsScreen()
            }
            composable(BottomNavItem.Budget.route) {
                BudgetScreen()
            }
            composable(BottomNavItem.More.route) {
                MoreScreen(
                    onAccountsClick = { navController.navigate("accounts") },
                    onCategoriesClick = { navController.navigate("categories") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("accounts") {
                AccountsScreen()
            }
            composable("categories") {
                CategoriesScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun MoreScreen(
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "More",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    MoreItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Accounts",
                        subtitle = "Manage your accounts",
                        onClick = onAccountsClick
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    MoreItem(
                        icon = Icons.Default.Category,
                        title = "Categories",
                        subtitle = "Manage categories",
                        onClick = onCategoriesClick
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    MoreItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "App preferences & data",
                        onClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
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
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
    }
}
