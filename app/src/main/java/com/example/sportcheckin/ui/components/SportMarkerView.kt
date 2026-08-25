package com.example.sportcheckin.ui.components

import android.content.Context
import android.widget.TextView
import com.example.sportcheckin.R
import com.example.sportcheckin.model.ChartPoint
import com.example.sportcheckin.util.toDurationText
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class SportMarkerView(
    context: Context,
    private val points: List<ChartPoint>
) : MarkerView(context, R.layout.chart_marker_view) {
    private val titleView: TextView = findViewById(R.id.tv_marker_title)
    private val valueView: TextView = findViewById(R.id.tv_marker_value)

    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        val point = entry?.x?.toInt()?.let(points::getOrNull)
        if (point != null) {
            titleView.text = point.fullLabel
            valueView.text = point.minutes.toDurationText()
        }
        super.refreshContent(entry, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -(height + 18f))
    }
}
