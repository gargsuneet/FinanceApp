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

val CURRENCIES = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "MXN",
    "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "AED")

val ACCOUNT_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
    "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FF9800", "#FF5722", "#795548", "#607D8B", "#F06292", "#AED581", "#4DB6AC", "#80DEEA"
)

val CATEGORY_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
    "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FF9800", "#FF5722", "#795548", "#607D8B", "#F06292", "#AED581", "#4DB6AC", "#80DEEA"
)

val CATEGORY_ICONS: List<String> = listOf(
    "restaurant", "directions_car", "shopping_cart", "movie", "receipt",
    "local_hospital", "school", "flight", "spa", "home",
    "fitness_center", "card_giftcard", "more_horiz", "work", "laptop",
    "trending_up", "apartment", "redeem", "attach_money", "payments",
    "account_balance", "credit_card", "savings", "category", "food_bank",
    "local_cafe", "local_bar", "directions_bus", "directions_bike", "train",
    "local_taxi", "shopping_bag", "local_mall", "tv", "sports_esports",
    "music_note", "electric_bolt", "water_drop", "wifi", "phone",
    "healing", "medication", "menu_book", "science", "beach_access",
    "hotel", "park", "sports", "sports_soccer", "volunteer_activism"
)

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon circle
        val iconColor = parseColor(
            if (transaction.categoryColor.isNotBlank()) transaction.categoryColor
            else when (transaction.type) {
                TransactionType.INCOME -> "#43A047"
                TransactionType.EXPENSE -> "#E53935"
                TransactionType.TRANSFER -> "#1E88E5"
            }
        )
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIconVector(transaction.categoryIcon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        // Center info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                when (transaction.type) {
                    TransactionType.TRANSFER -> "Transfer"
                    else -> transaction.categoryName.ifBlank { transaction.note.ifBlank { transaction.type.name } }
                },
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF212121)
            )
            if (transaction.subcategoryName.isNotEmpty()) {
                Text(
                    transaction.subcategoryName,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Text(
                buildString {
                    when (transaction.type) {
                        TransactionType.TRANSFER -> {
                            append(transaction.accountName)
                            if (transaction.toAccountName.isNotBlank()) append(" → ${transaction.toAccountName}")
                        }
                        else -> {
                            append(transaction.accountName)
                            if (transaction.note.isNotEmpty()) append(" · ${transaction.note}")
                        }
                    }
                },
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        // Right amount + time
        Column(horizontalAlignment = Alignment.End) {
            val amtColor = when (transaction.type) {
                TransactionType.EXPENSE -> Color(0xFFE53935)
                TransactionType.INCOME -> Color(0xFF43A047)
                TransactionType.TRANSFER -> Color(0xFF1E88E5)
            }
            val amtText = when (transaction.type) {
                TransactionType.EXPENSE -> "-${transaction.currency} ${String.format("%.2f", transaction.amount)}"
                TransactionType.INCOME -> "+${transaction.currency} ${String.format("%.2f", transaction.amount)}"
                TransactionType.TRANSFER -> "${transaction.currency} ${String.format("%.2f", transaction.amount)}"
            }
            Text(
                amtText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = amtColor
            )
            Text(
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(transaction.date)),
                fontSize = 11.sp,
                color = Color(0xFFBDBDBD)
            )
            if (transaction.photoUri != null) {
                Icon(
                    Icons.Default.CameraAlt, contentDescription = "Has photo",
                    tint = Color(0xFFBDBDBD), modifier = Modifier.size(12.dp)
                )
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00897B))
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
        "more_horiz" -> Icons.Default.MoreHoriz
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
        "food_bank" -> Icons.Default.FoodBank
        "local_cafe" -> Icons.Default.LocalCafe
        "local_bar" -> Icons.Default.LocalBar
        "directions_bus" -> Icons.Default.DirectionsBus
        "directions_bike" -> Icons.Default.DirectionsBike
        "train" -> Icons.Default.Train
        "local_taxi" -> Icons.Default.LocalTaxi
        "shopping_bag" -> Icons.Default.ShoppingBag
        "local_mall" -> Icons.Default.LocalMall
        "tv" -> Icons.Default.Tv
        "sports_esports" -> Icons.Default.SportsEsports
        "music_note" -> Icons.Default.MusicNote
        "electric_bolt" -> Icons.Default.ElectricBolt
        "water_drop" -> Icons.Default.WaterDrop
        "wifi" -> Icons.Default.Wifi
        "phone" -> Icons.Default.Phone
        "healing" -> Icons.Default.Healing
        "medication" -> Icons.Default.Medication
        "menu_book" -> Icons.Default.MenuBook
        "science" -> Icons.Default.Science
        "beach_access" -> Icons.Default.BeachAccess
        "hotel" -> Icons.Default.Hotel
        "park" -> Icons.Default.Park
        "sports" -> Icons.Default.Sports
        "sports_soccer" -> Icons.Default.SportsSoccer
        "volunteer_activism" -> Icons.Default.VolunteerActivism
        else -> Icons.Default.Category
    }
}
