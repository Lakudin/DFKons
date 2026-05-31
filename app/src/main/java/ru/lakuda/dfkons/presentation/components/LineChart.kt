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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    areaColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    pointColor: Color = MaterialTheme.colorScheme.primary,
    showPoints: Boolean = false,
    stepLine: Boolean = false,
    smoothLine: Boolean = true,
    /** Нижняя граница оси Y = 0, линия начинается с фактического баланса */
    yAxisFromZero: Boolean = false,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxValue = data.maxOrNull() ?: 1f
    val dataMin = data.minOrNull() ?: 0f
    val minValue = if (yAxisFromZero) 0f else dataMin
    val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingTop = 20f
        val paddingBottom = 40f
        val paddingLeft = 72f
        val chartHeight = height - paddingTop - paddingBottom
        val chartWidth = width - paddingLeft

        val chartPoints = data.indices.map { i ->
            getPoint(i, data[i], chartWidth, chartHeight, paddingTop, paddingLeft, maxValue, minValue, valueRange, data.size)
        }

        val linePath = when {
            stepLine -> buildStepPath(chartPoints)
            smoothLine -> buildSmoothPath(chartPoints)
            else -> buildPolylinePath(chartPoints)
        }
        // Низ графика = 0 на оси Y (заливка до нуля, линия — с первой суммы)
        val zeroY = paddingTop + chartHeight

        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(chartPoints.last().x, zeroY)
            lineTo(chartPoints.first().x, zeroY)
            close()
        }

        // Сетка и подписи оси Y
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = paddingTop + (chartHeight / gridLines) * i
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            val value = maxValue - (valueRange / gridLines) * i
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(
                    String.format("%.0f", value),
                    paddingLeft - 8f,
                    y + 8f,
                    paint
                )
            }
        }

        drawPath(path = areaPath, color = areaColor, style = Fill)
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        if (showPoints) {
            chartPoints.forEach { point ->
                drawCircle(color = pointColor, radius = 6f, center = point)
                drawCircle(color = Color.White, radius = 2.5f, center = point)
            }
        }

        // Подписи дат по оси X
        if (labels.isNotEmpty()) {
            val labelStep = (labels.size / 6).coerceAtLeast(1)
            for (i in labels.indices step labelStep) {
                val x = paddingLeft + (chartWidth / (data.size - 1).coerceAtLeast(1)) * i
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val label = if (labels[i].length > 5) labels[i].takeLast(5) else labels[i]
                    drawText(
                        label,
                        x,
                        height - 10,
                        paint
                    )
                }
            }
        }
    }
}

private fun getPoint(
    index: Int,
    value: Float,
    chartWidth: Float,
    chartHeight: Float,
    paddingTop: Float,
    paddingLeft: Float,
    maxValue: Float,
    minValue: Float,
    valueRange: Float,
    dataSize: Int
): Offset {
    val stepX = chartWidth / (dataSize - 1).coerceAtLeast(1)
    val x = paddingLeft + index * stepX
    val y = paddingTop + chartHeight - ((value - minValue) / valueRange) * chartHeight
    return Offset(x, y)
}

private fun buildStepPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        path.lineTo(curr.x, prev.y)
        path.lineTo(curr.x, curr.y)
    }
    return path
}

private fun buildPolylinePath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    return path
}

/** Плавная кривая; на участках с одинаковым значением — прямая (без провала). */
private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) {
        path.moveTo(points[0].x, points[0].y)
        return path
    }
    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        if (kotlin.math.abs(p1.y - p2.y) < 1f) {
            path.lineTo(p2.x, p2.y)
        } else {
            val p0 = points[(i - 1).coerceAtLeast(0)]
            val p3 = points[(i + 2).coerceAtMost(points.size - 1)]
            val cp1x = p1.x + (p2.x - p0.x) / 6f
            val cp1y = p1.y + (p2.y - p0.y) / 6f
            val cp2x = p2.x - (p3.x - p1.x) / 6f
            val cp2y = p2.y - (p3.y - p1.y) / 6f
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
        }
    }
    return path
}

@Composable
fun PieChart(
    data: Map<String, Double>,
    colors: List<Color> = listOf(
        Color(0xFF7A92C9),
        Color(0xFF03DAC6),
        Color(0xFFBB86FC),
        Color(0xFFFFB74D),
        Color(0xFFFF8A80),
        Color(0xFFA5D6A7)
    ),
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет данных для отображения",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = data.values.sum()

    Column(modifier = modifier.fillMaxWidth()) {
        // Круговая диаграмма
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2 - 40f

            var startAngle = -90f // Начинаем с верхней точки

            data.values.forEachIndexed { index, value ->
                val sweepAngle = (value / total).toFloat() * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }

        // Легенда
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.keys.forEachIndexed { index, category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size])
                    )
                    Text(
                        text = "$category: ${String.format("%.2f", data[category] ?: 0.0)} ₽",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LineChartPreview() {
    MaterialTheme {
        Column {
            LineChart(
                data = listOf(1000f, 1500f, 800f, 1200f, 2000f, 1700f, 900f),
                labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}