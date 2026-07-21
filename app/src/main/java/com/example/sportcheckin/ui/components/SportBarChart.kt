package com.example.sportcheckin.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun SportBarChart(
    points: List<ChartPoint>,
    dimension: StatsDimension,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry?, highlight: Highlight?) {
                        val index = entry?.x?.toInt() ?: return
                        val point = points.getOrNull(index) ?: return
                        Toast.makeText(
                            context,
                            "${point.fullLabel}：${point.minutes}分钟",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onNothingSelected() = Unit
                })
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
