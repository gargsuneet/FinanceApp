package com.financeapp.presentation.components

import androidx.compose.ui.graphics.Color

object FinanceTheme {
    val Income     = Color(0xFF43A047)   // AndroMoney green
    val Expense    = Color(0xFFE53935)   // AndroMoney red
    val Transfer   = Color(0xFF1E88E5)   // AndroMoney blue
    val Background = Color(0xFFF5F5F5)
    val CardBackground = Color(0xFFFFFFFF)
    val Primary    = Color(0xFF00897B)   // AndroMoney teal
    val PrimaryDark= Color(0xFF00695C)
    val PrimaryVariant = Color(0xFF00695C)
    val Secondary  = Color(0xFF00897B)
    val Surface    = Color(0xFFFFFFFF)
    val OnPrimary  = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF212121)
    val OnSurface  = Color(0xFF212121)
    val Divider    = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF757575)
    val AmOrange   = Color(0xFFFF6F00)
}

val CURRENCIES = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "BRL", "MXN", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "RUB")

val CATEGORY_ICONS = listOf(
    "restaurant", "directions_car", "shopping_cart", "movie", "receipt",
    "local_hospital", "school", "flight", "spa", "home", "fitness_center",
    "card_giftcard", "more_horiz", "work", "laptop", "trending_up",
    "apartment", "redeem", "attach_money", "sports_esports", "music_note",
    "pets", "child_care", "local_grocery_store", "local_cafe", "local_bar",
    "commute", "subway", "local_taxi", "local_gas_station", "electric_bolt",
    "water_drop", "wifi", "phone", "computer", "tv", "book", "camera_alt",
    "checkroom", "diamond", "savings", "account_balance", "credit_card"
)

val ACCOUNT_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
    "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FFC107", "#FF9800", "#FF5722", "#795548",
    "#607D8B", "#9E9E9E", "#000000"
)
