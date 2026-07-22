package com.example.sportcheckin.ui.stats

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
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
import com.example.sportcheckin.ui.components.WheelTimePicker
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
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingHistoryItem by remember { mutableStateOf<DayDetailItem?>(null) }
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
                key(ui.dimension) {
                    SportBarChart(
                        points = ui.chartPoints,
                        dimension = ui.dimension,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                }
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
                onClose = { viewModel.selectDate(null) },
                onToggle = { item, checked -> viewModel.setHistoryCompleted(item, checked) },
                onEditMinutes = { item -> editingHistoryItem = item }
            )
        }
    }

    editingHistoryItem?.let { item ->
        HistoryRecordEditorDialog(
            item = item,
            onDismiss = { editingHistoryItem = null },
            onConfirm = { minutes ->
                viewModel.updateHistoryMinutes(item.projectId, minutes)
                editingHistoryItem = null
            }
        )
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
    onClose: () -> Unit,
    onToggle: (DayDetailItem, Boolean) -> Unit,
    onEditMinutes: (DayDetailItem) -> Unit
) {
    val date = ui.selectedDate ?: return
    val canEdit = !date.isAfter(LocalDate.now())
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
        Text(
            text = if (canEdit) "可勾选补卡；点击“改时间”会把该项目标记为已完成并写入分钟数" else "未来日期不可补卡",
            style = MaterialTheme.typography.bodyMedium,
            color = if (canEdit) MintGreen else MaterialTheme.colorScheme.error
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
                    DayDetailRow(
                        item = item,
                        enabled = canEdit,
                        onToggle = { checked -> onToggle(item, checked) },
                        onEditMinutes = { onEditMinutes(item) }
                    )
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
private fun DayDetailRow(
    item: DayDetailItem,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEditMinutes: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onToggle(!item.completed) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = onToggle,
                enabled = enabled
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (item.completed) "已完成 ${item.minutes.toDurationText()}" else "未完成 · 目标 ${item.minutes.toDurationText()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            TextButton(onClick = onEditMinutes, enabled = enabled) {
                Text(text = "改时间")
            }
        }
    }
}

@Composable
private fun HistoryRecordEditorDialog(
    item: DayDetailItem,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit
) {
    var hour by remember { mutableIntStateOf((item.minutes.coerceIn(1, 1440)) / 60) }
    var minute by remember { mutableIntStateOf((item.minutes.coerceIn(1, 1440)) % 60) }
    val totalMinutes = hour * 60 + minute

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "修改历史记录") },
        text = {
            Column {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "保存后该项目在当天会标记为已完成。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                WheelTimePicker(
                    initialHour = hour,
                    initialMinute = minute,
                    modifier = Modifier.fillMaxWidth()
                ) { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "合计：${totalMinutes.toDurationText()}（范围 1 ~ 1440 分钟）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = totalMinutes in 1..1440,
                onClick = { onConfirm(totalMinutes) }
            ) {
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        }
    )
}
