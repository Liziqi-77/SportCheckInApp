package com.example.sportcheckin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportcheckin.model.DayCell
import com.example.sportcheckin.model.DayStatus
import com.example.sportcheckin.model.MonthHeat
import com.example.sportcheckin.ui.theme.EmptyGray
import com.example.sportcheckin.ui.theme.MintGreen
import com.example.sportcheckin.ui.theme.TextPrimary
import com.example.sportcheckin.ui.theme.TextSecondary
import com.example.sportcheckin.ui.theme.WarningYellow
import java.time.LocalDate

private val weekTitles = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun CalendarStatusLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = MintGreen, text = "全部完成")
        LegendItem(color = WarningYellow, text = "部分完成")
        LegendItem(color = EmptyGray, text = "无记录")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun MonthCalendarGrid(
    cells: List<DayCell>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekTitles.forEach { title ->
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(286.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(cells) { cell ->
                DayCellView(
                    cell = cell,
                    selected = cell.date != null && cell.date == selectedDate,
                    onClick = { cell.date?.let(onDateClick) }
                )
            }
        }
    }
}

@Composable
private fun DayCellView(
    cell: DayCell,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (cell.date == null) {
        Box(modifier = Modifier.size(38.dp))
        return
    }
    val statusColor = when (cell.status) {
        DayStatus.ALL -> MintGreen
        DayStatus.PARTIAL -> WarningYellow
        DayStatus.NONE -> EmptyGray
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else statusColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.date.dayOfMonth.toString(),
            color = if (selected) Color.White else TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun YearHeatGrid(
    months: List<MonthHeat>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .height(560.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { month ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${month.month.monthValue}月",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    month.days.chunked(7).forEach { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            week.forEach { day ->
                                val color = when (day.status) {
                                    DayStatus.ALL -> MintGreen
                                    DayStatus.PARTIAL -> WarningYellow
                                    DayStatus.NONE -> EmptyGray
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                        .clickable(enabled = day.date != null) {
                                            day.date?.let(onDateClick)
                                        }
                                )
                            }
                            repeat(7 - week.size) {
                                Box(modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
