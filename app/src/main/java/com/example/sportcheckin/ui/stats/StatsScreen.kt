package com.example.sportcheckin.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportcheckin.R
import com.example.sportcheckin.model.CalendarMode
import com.example.sportcheckin.model.DayDetailItem
import com.example.sportcheckin.model.StatsDimension
import com.example.sportcheckin.model.StatsUiState
import com.example.sportcheckin.ui.components.CalendarStatusLegend
import com.example.sportcheckin.ui.components.MonthCalendarGrid
import com.example.sportcheckin.ui.components.SportBarChart
import com.example.sportcheckin.ui.components.YearHeatGrid
import com.example.sportcheckin.ui.theme.BrandOrange
import com.example.sportcheckin.ui.theme.EmptyGray
import com.example.sportcheckin.ui.theme.MintGreen
import com.example.sportcheckin.ui.theme.TextPrimary
import com.example.sportcheckin.ui.theme.TextSecondary
import com.example.sportcheckin.ui.theme.WarningYellow
import com.example.sportcheckin.util.DateUtils
import com.example.sportcheckin.util.toDurationText
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ui = state

    if (ui == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandOrange)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 运动统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ModeSwitch(
                selected = ui.calendarMode,
                onSelected = viewModel::setCalendarMode
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (ui.calendarMode == CalendarMode.MONTH) {
                    MonthCalendarGrid(
                        cells = ui.monthCells,
                        selectedDate = ui.selectedDate,
                        onDateClick = viewModel::selectDate
                    )
                } else {
                    YearHeatGrid(
                        months = ui.yearHeat,
                        onDateClick = viewModel::selectDate
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                CalendarStatusLegend()
            }
        }

        DimensionSelector(
            selected = ui.dimension,
            onSelected = viewModel::setDimension
        )

        PeriodNavigator(
            periodLabel = ui.periodLabel,
            onPrevious = { viewModel.moveAnchor(-1) },
            onNext = { viewModel.moveAnchor(1) },
            onToday = viewModel::goToday
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = chartTitle(ui.dimension),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                SportBarChart(
                    points = ui.chartPoints,
                    dimension = ui.dimension,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }

        SummaryCard(ui = ui)
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (ui.selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectDate(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DayDetailSheet(
                ui = ui,
                onClose = { viewModel.selectDate(null) }
            )
        }
    }
}

@Composable
private fun ModeSwitch(
    selected: CalendarMode,
    onSelected: (CalendarMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            CalendarMode.entries.forEach { mode ->
                val isSelected = mode == selected
                TextButton(
                    onClick = { onSelected(mode) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) BrandOrange else Color.Transparent)
                ) {
                    Text(
                        text = mode.label,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DimensionSelector(
    selected: StatsDimension,
    onSelected: (StatsDimension) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsDimension.entries.forEach { dimension ->
            val isSelected = dimension == selected
            TextButton(
                onClick = { onSelected(dimension) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) BrandOrange else MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = dimension.label,
                    color = if (isSelected) Color.White else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun PeriodNavigator(
    periodLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious) { Text(text = "‹ 上一段") }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = periodLabel, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onToday) { Text(text = "回到今天") }
        }
        TextButton(onClick = onNext) { Text(text = "下一段 ›") }
    }
}

private fun chartTitle(dimension: StatsDimension): String {
    return when (dimension) {
        StatsDimension.WEEK -> "周视图 · 每日总时长（7 根柱子）"
        StatsDimension.MONTH -> "月视图 · 每日总时长（可横向滚动/双指缩放）"
        StatsDimension.QUARTER -> "季度视图 · 按周聚合（约 13 根柱子）"
        StatsDimension.YEAR -> "年视图 · 按月聚合（12 根柱子）"
    }
}

@Composable
private fun SummaryCard(ui: StatsUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                iconRes = R.drawable.ic_trophy_24,
                title = "🏆 总时长",
                value = ui.summary.totalMinutes.toDurationText()
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(54.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
            SummaryItem(
                iconRes = R.drawable.ic_timer_24,
                title = "⏱️ 日均时长",
                value = String.format(Locale.CHINA, "%.1f 分钟/天", ui.summary.averageMinutesPerDay)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    iconRes: Int,
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun DayDetailSheet(
    ui: StatsUiState,
    onClose: () -> Unit
) {
    val date = ui.selectedDate ?: return
    val completedCount = ui.selectedDayDetails.count { it.completed }
    val statusColor = when {
        ui.projectCount == 0 || completedCount == 0 -> EmptyGray
        completedCount >= ui.projectCount -> MintGreen
        else -> WarningYellow
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${DateUtils.dayFormatter.format(date)} 打卡明细",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "已完成 $completedCount / ${ui.projectCount} 项",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (ui.selectedDayDetails.isEmpty()) {
            Text(
                text = "暂无运动项目，请先在首页添加。",
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 28.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ui.selectedDayDetails, key = { it.projectId }) { item ->
                    DayDetailRow(item = item)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                Text(text = "关闭")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DayDetailRow(item: DayDetailItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (item.completed) MintGreen else EmptyGray)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (item.completed) "已完成 ${item.minutes.toDurationText()}" else "未完成 · 目标 ${item.minutes.toDurationText()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = if (item.completed) "✓" else "—",
                color = if (item.completed) MintGreen else TextSecondary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
