package ru.lakuda.dfkons.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// НЕ определяем formatLargeNumber здесь - используем из CombinedLineChart.kt

@Composable
fun SeparateCharts(
    dailyTransactions: Map<String, Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    if (dailyTransactions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp
    val axisTextSize = when {
        screenWidthDp < 360 -> 8f
        screenWidthDp < 400 -> 9f
        else -> 10f
    }

    val sortedDates = dailyTransactions.keys.sortedWith(compareBy {
        val parts = it.split(".")
        if (parts.size == 2) (parts[1] + parts[0]).toIntOrNull() ?: 0 else 0
    })

    val expenses = sortedDates.map { abs(dailyTransactions[it]?.first ?: 0.0).toFloat() }
    val incomes = sortedDates.map { abs(dailyTransactions[it]?.second ?: 0.0).toFloat() }

    val maxExpense = if (expenses.isNotEmpty()) expenses.max()!! else 1f
    val maxIncome = if (incomes.isNotEmpty()) incomes.max()!! else 1f

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // График расходов
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📉 Расходы",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Всего: ${formatShortNumber(expenses.sum().toDouble())} ₽",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SingleLineChart(
                    data = expenses,
                    labels = sortedDates,
                    color = MaterialTheme.colorScheme.error,
                    maxValue = maxExpense,
                    axisTextSize = axisTextSize,
                    density = density
                )
            }
        }

        // График доходов
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 Доходы",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Всего: ${formatShortNumber(incomes.sum().toDouble())} ₽",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SingleLineChart(
                    data = incomes,
                    labels = sortedDates,
                    color = Color(0xFF4CAF50),
                    maxValue = maxIncome,
                    axisTextSize = axisTextSize,
                    density = density
                )
            }
        }
    }
}

@Composable
fun SingleLineChart(
    data: List<Float>,
    labels: List<String>,
    color: Color,
    maxValue: Float,
    axisTextSize: Float,
    density: androidx.compose.ui.unit.Density
) {
    if (data.isEmpty() || data.size < 2) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Text("Недостаточно данных", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val minValue = 0f
    val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)
        val paddingTop = 20f
        val paddingBottom = 25f
        val chartHeight = height - paddingTop - paddingBottom
        val zeroY = paddingTop + chartHeight

        // Цвет для линий сетки
        val gridLineColor = Color(0xFFE0E0E0)

        // Сетка (3 линии)
        for (i in 0..3) {
            val y = paddingTop + (chartHeight / 3) * i
            drawLine(
                color = gridLineColor,
                start = Offset(x = 0f, y = y),
                end = Offset(x = width, y = y),
                strokeWidth = 1f
            )
            val value = maxValue - (maxValue / 3) * i
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = axisTextSize * density.density
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                // Используем formatLargeNumber из CombinedLineChart.kt
                drawText(formatLargeNumber(value), 40f, y + 5, paint)
            }
        }

        drawLine(
            color = gridLineColor,
            start = Offset(x = 0f, y = zeroY),
            end = Offset(x = width, y = zeroY),
            strokeWidth = 1.5f
        )

        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = paddingTop + chartHeight - ((value - minValue) / valueRange) * chartHeight
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            val areaPath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                lineTo(points.last().x, zeroY)
                lineTo(points.first().x, zeroY)
                close()
            }
            drawPath(path = areaPath, color = color.copy(alpha = 0.15f), style = Fill)
        }

        if (points.size > 1) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
            }
            drawPath(path = linePath, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }

        points.forEach { point ->
            drawCircle(color = color, radius = 5f, center = point)
            drawCircle(color = Color.White, radius = 2f, center = point)
        }

        for (i in labels.indices step 2) {
            val x = stepX * i
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = (axisTextSize - 1) * density.density
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(labels[i], x, height - 8, paint)
            }
        }
    }
}

private fun formatShortNumber(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.1fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}