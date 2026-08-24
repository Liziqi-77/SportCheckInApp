package com.example.sportcheckin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportcheckin.model.ChartPoint
import com.example.sportcheckin.model.StatsDimension
import com.example.sportcheckin.ui.theme.BrandOrange
import com.example.sportcheckin.ui.theme.TextSecondary
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun SportBarChart(
    points: List<ChartPoint>,
    dimension: StatsDimension,
    modifier: Modifier = Modifier
) {
    val barColor = BrandOrange.toArgb()
    val valueColor = TextSecondary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            BarChart(viewContext).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setTouchEnabled(true)
                isDragEnabled = true
                axisRight.isEnabled = false
                axisLeft.apply {
                    axisMinimum = 0f
                    textColor = valueColor
                    gridColor = 0x226B7280
                }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = valueColor
                }
                setExtraOffsets(8f, 8f, 8f, 16f)
            }
        },
        update = { chart ->
            val entries = points.mapIndexed { index, point ->
                BarEntry(index.toFloat(), point.minutes.toFloat())
            }
            val dataSet = BarDataSet(entries, "运动时长").apply {
                color = barColor
                valueTextColor = valueColor
                valueTextSize = 10f
                highLightColor = 0xFFFFC107.toInt()
            }
            chart.marker = SportMarkerView(chart.context, points)
            chart.data = BarData(dataSet).apply { barWidth = 0.62f }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(points.map { it.label })
            chart.xAxis.setLabelCount(points.size.coerceAtLeast(1), false)

            when (dimension) {
                StatsDimension.MONTH -> {
                    chart.setScaleEnabled(true)
                    chart.setPinchZoom(true)
                    chart.setVisibleXRangeMaximum(14f)
                    chart.moveViewToX(0f)
                }

                else -> {
                    chart.setScaleEnabled(false)
                    chart.setPinchZoom(false)
                    chart.fitScreen()
                }
            }

            chart.notifyDataSetChanged()
            chart.animateY(420)
            chart.invalidate()
        }
    )
}
