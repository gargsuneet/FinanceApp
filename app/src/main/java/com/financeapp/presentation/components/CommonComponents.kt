package com.financeapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.domain.model.Transaction
import com.financeapp.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon circle
            val iconColor = parseColor(transaction.categoryColor.ifBlank {
                when (transaction.type) {
                    TransactionType.INCOME -> "#4CAF50"
                    TransactionType.EXPENSE -> "#F44336"
                    TransactionType.TRANSFER -> "#2196F3"
                }
            })
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIconVector(transaction.categoryIcon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.TRANSFER -> "Transfer: ${transaction.accountName} → ${transaction.toAccountName}"
                        else -> transaction.categoryName.ifBlank { transaction.note.ifBlank { transaction.type.name } }
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
                Text(
                    text = "${transaction.accountName} • ${formatDate(transaction.date)}",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val amountColor = when (transaction.type) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> Color(0xFFF44336)
                    TransactionType.TRANSFER -> Color(0xFF2196F3)
                }
                val prefix = when (transaction.type) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.TRANSFER -> "→"
                }
                Text(
                    text = "$prefix${formatAmount(transaction.amount, transaction.currency)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = amountColor
                )
                if (transaction.fee > 0) {
                    Text(
                        text = "Fee: ${formatAmount(transaction.fee, transaction.currency)}",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
                if (transaction.points > 0) {
                    Text(
                        text = "Pts: ${transaction.points.toInt()}",
                        fontSize = 10.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    income: Double,
    expense: Double,
    balance: Double,
    currency: String = "USD",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Monthly Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Text(
                formatAmount(balance, currency),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Text(formatAmount(income, currency), color = Color(0xFF81C784), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Expense", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Text(formatAmount(expense, currency), color = Color(0xFFEF9A9A), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

fun formatAmount(amount: Double, currency: String = "USD"): String {
    return try {
        val locale = when (currency) {
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            else -> Locale.US
        }
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(currency)
        format.format(amount)
    } catch (e: Exception) {
        "$currency ${String.format("%.2f", amount)}"
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF9C27B0)
    }
}

fun categoryIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_cart" -> Icons.Default.ShoppingCart
        "movie" -> Icons.Default.Movie
        "receipt" -> Icons.Default.Receipt
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "flight" -> Icons.Default.Flight
        "spa" -> Icons.Default.Spa
        "home" -> Icons.Default.Home
        "fitness_center" -> Icons.Default.FitnessCenter
        "card_giftcard" -> Icons.Default.CardGiftcard
        "work" -> Icons.Default.Work
        "laptop" -> Icons.Default.Laptop
        "trending_up" -> Icons.Default.TrendingUp
        "apartment" -> Icons.Default.Apartment
        "redeem" -> Icons.Default.Redeem
        "attach_money" -> Icons.Default.AttachMoney
        "payments" -> Icons.Default.Payments
        "account_balance" -> Icons.Default.AccountBalance
        "credit_card" -> Icons.Default.CreditCard
        "savings" -> Icons.Default.Savings
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        "transfer_within_a_station" -> Icons.Default.SwapHoriz
        "swap_horiz" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}
