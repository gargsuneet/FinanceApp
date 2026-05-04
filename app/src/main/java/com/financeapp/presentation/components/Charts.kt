package com.financeapp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.domain.model.CategorySpending
import com.financeapp.domain.model.MonthlyTrend
import com.financeapp.domain.model.MonthlySummary
import kotlin.math.min

@Composable
fun PieChart(
    data: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.amount }
    val colors = data.map { parseColor(it.categoryColor) }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)) {
            val diameter = min(size.width, size.height) * 0.85f
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var startAngle = -90f

            data.forEachIndexed { index, item ->
                val sweepAngle = (item.amount / total * 360f).toFloat()
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = 2f)
                )
                startAngle += sweepAngle
            }
            // Center hole
            drawCircle(
                color = Color.White,
                radius = diameter / 4f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(data.take(8)) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = parseColor(item.categoryColor))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${item.categoryName} ${String.format("%.1f", item.percentage)}%",
                        fontSize = 11.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

@Composable
fun BarChart(
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) return

    val maxVal = summaries.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1.0)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Column(modifier = modifier) {
        Text("Income vs Expense (6 months)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            summaries.forEach { summary ->
                val incomeHeight = (summary.income / maxVal * 130).toFloat()
                val expenseHeight = (summary.expense / maxVal * 130).toFloat()
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Canvas(modifier = Modifier.width(14.dp).height(incomeHeight.dp.coerceAtLeast(4.dp))) {
                            drawRect(color = Color(0xFF4CAF50))
                        }
                        Canvas(modifier = Modifier.width(14.dp).height(expenseHeight.dp.coerceAtLeast(4.dp))) {
                            drawRect(color = Color(0xFFF44336))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(months.getOrNull(summary.month - 1) ?: "", fontSize = 10.sp, color = Color(0xFF757575))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) { drawRect(Color(0xFF4CAF50)) }
                Spacer(Modifier.width(4.dp))
                Text("Income", fontSize = 11.sp, color = Color(0xFF757575))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) { drawRect(Color(0xFFF44336)) }
                Spacer(Modifier.width(4.dp))
                Text("Expense", fontSize = 11.sp, color = Color(0xFF757575))
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    budget: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val barColor = when {
        progress >= 1f -> Color(0xFFF44336)
        progress >= 0.8f -> Color(0xFFFF9800)
        else -> color
    }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatAmount(spent),
                fontSize = 12.sp,
                color = barColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                formatAmount(budget),
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            drawRoundRect(
                color = Color(0xFFE0E0E0),
                size = Size(size.width, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = barColor,
                size = Size(size.width * progress, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
fun TrendChart(data: List<MonthlyTrend>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxVal = data.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1.0)
    Column(modifier = modifier) {
        Text("Income vs Expense Trend", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
        Spacer(Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val w = size.width
            val h = size.height - 24.dp.toPx()
            val stepX = if (data.size > 1) w / (data.size - 1) else w
            repeat(4) { i ->
                val y = h * i / 3f
                drawLine(Color(0xFFEEEEEE), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            for (i in 1 until data.size) {
                val x1 = stepX * (i - 1)
                val y1 = h - (data[i-1].income / maxVal * h).toFloat()
                val x2 = stepX * i
                val y2 = h - (data[i].income / maxVal * h).toFloat()
                drawLine(Color(0xFF4CAF50), Offset(x1, y1), Offset(x2, y2), strokeWidth = 3f)
            }
            for (i in 1 until data.size) {
                val x1 = stepX * (i - 1)
                val y1 = h - (data[i-1].expense / maxVal * h).toFloat()
                val x2 = stepX * i
                val y2 = h - (data[i].expense / maxVal * h).toFloat()
                drawLine(Color(0xFFF44336), Offset(x1, y1), Offset(x2, y2), strokeWidth = 3f)
            }
            data.forEachIndexed { i, item ->
                val x = stepX * i
                val yIncome = h - (item.income / maxVal * h).toFloat()
                val yExpense = h - (item.expense / maxVal * h).toFloat()
                drawCircle(Color(0xFF4CAF50), radius = 5f, center = Offset(x, yIncome))
                drawCircle(Color(0xFFF44336), radius = 5f, center = Offset(x, yExpense))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { item -> Text(item.monthLabel, fontSize = 10.sp, color = Color(0xFF757575)) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) { drawCircle(Color(0xFF4CAF50)) }
                Spacer(Modifier.width(4.dp))
                Text("Income", fontSize = 11.sp, color = Color(0xFF757575))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) { drawCircle(Color(0xFFF44336)) }
                Spacer(Modifier.width(4.dp))
                Text("Expense", fontSize = 11.sp, color = Color(0xFF757575))
            }
        }
    }
}
