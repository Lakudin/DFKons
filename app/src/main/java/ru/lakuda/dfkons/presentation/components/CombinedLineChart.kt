package ru.lakuda.dfkons.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

fun formatLargeNumber(value: Float): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
        value >= 10_000 -> String.format("%.0fK", value / 1_000)
        value >= 1_000 -> String.format("%.1fK", value / 1_000)
        else -> String.format("%.0f", value)
    }
}

@Composable
fun CombinedLineChart(
    data: Map<String, Pair<Double, Double>>, // date -> (expense, income)
    expenseColor: Color = MaterialTheme.colorScheme.error,
    incomeColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет данных для отображения",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

    val sortedDates = data.keys.sortedWith(compareBy {
        val parts = it.split(".")
        if (parts.size == 2) {
            (parts[1] + parts[0]).toIntOrNull() ?: 0
        } else 0
    })

    val expenseValues = sortedDates.map { abs(data[it]?.first ?: 0.0).toFloat() }
    val incomeValues = sortedDates.map { abs(data[it]?.second ?: 0.0).toFloat() }

    val allValues = expenseValues + incomeValues
    val maxValue = (allValues.maxOrNull() ?: 1.0).toFloat()
    val minValue = 0f
    val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (expenseValues.size - 1).coerceAtLeast(1)
        val paddingTop = 40f
        val paddingBottom = 50f
        val chartHeight = height - paddingTop - paddingBottom
        val zeroY = paddingTop + chartHeight

        val gridLines = 5
        for (i in 0..gridLines) {
            val y = paddingTop + (chartHeight / gridLines) * i
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(x = 0f, y = y),
                end = Offset(x = width, y = y),
                strokeWidth = 1f
            )

            val value = maxValue - (valueRange / gridLines) * i
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = axisTextSize * density.density
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(
                    formatLargeNumber(value),
                    50f,
                    y + 10,
                    paint
                )
            }
        }

        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(x = 0f, y = zeroY),
            end = Offset(x = width, y = zeroY),
            strokeWidth = 2f
        )

        val expensePoints = expenseValues.mapIndexed { index, value ->
            val x = index * stepX
            val y = paddingTop + chartHeight - ((value - minValue) / valueRange) * chartHeight
            Offset(x, y)
        }

        val incomePoints = incomeValues.mapIndexed { index, value ->
            val x = index * stepX
            val y = paddingTop + chartHeight - ((value - minValue) / valueRange) * chartHeight
            Offset(x, y)
        }

        if (expensePoints.isNotEmpty()) {
            val expenseAreaPath = Path().apply {
                moveTo(expensePoints.first().x, expensePoints.first().y)
                for (i in 1 until expensePoints.size) {
                    lineTo(expensePoints[i].x, expensePoints[i].y)
                }
                lineTo(expensePoints.last().x, zeroY)
                lineTo(expensePoints.first().x, zeroY)
                close()
            }

            drawPath(
                path = expenseAreaPath,
                color = expenseColor.copy(alpha = 0.15f),
                style = Fill
            )
        }

        if (incomePoints.isNotEmpty()) {
            val incomeAreaPath = Path().apply {
                moveTo(incomePoints.first().x, incomePoints.first().y)
                for (i in 1 until incomePoints.size) {
                    lineTo(incomePoints[i].x, incomePoints[i].y)
                }
                lineTo(incomePoints.last().x, zeroY)
                lineTo(incomePoints.first().x, zeroY)
                close()
            }

            drawPath(
                path = incomeAreaPath,
                color = incomeColor.copy(alpha = 0.15f),
                style = Fill
            )
        }

        if (expensePoints.size > 1) {
            val expenseLinePath = Path().apply {
                moveTo(expensePoints.first().x, expensePoints.first().y)
                for (i in 1 until expensePoints.size) {
                    lineTo(expensePoints[i].x, expensePoints[i].y)
                }
            }

            drawPath(
                path = expenseLinePath,
                color = expenseColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        if (incomePoints.size > 1) {
            val incomeLinePath = Path().apply {
                moveTo(incomePoints.first().x, incomePoints.first().y)
                for (i in 1 until incomePoints.size) {
                    lineTo(incomePoints[i].x, incomePoints[i].y)
                }
            }

            drawPath(
                path = incomeLinePath,
                color = incomeColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        expensePoints.forEach { point ->
            drawCircle(
                color = expenseColor,
                radius = 6f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = point
            )
        }

        incomePoints.forEach { point ->
            drawCircle(
                color = incomeColor,
                radius = 6f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = point
            )
        }

        for (i in sortedDates.indices) {
            val x = stepX * i
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = axisTextSize * density.density
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val label = sortedDates[i]
                drawText(
                    label,
                    x,
                    height - 15,
                    paint
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(expenseColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Расходы",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(incomeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Доходы",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}