package com.example.sportcheckin.model

import java.time.LocalDate
import java.time.YearMonth

enum class CalendarMode(val label: String) {
    MONTH("月视图"),
    YEAR("年视图")
}

enum class StatsDimension(val label: String) {
    WEEK("周"),
    MONTH("月"),
    QUARTER("季"),
    YEAR("年")
}

enum class DayStatus {
    NONE,
    PARTIAL,
    ALL
}

data class DayCell(
    val date: LocalDate?,
    val inCurrentPeriod: Boolean,
    val status: DayStatus,
    val totalMinutes: Int,
    val completedCount: Int,
    val projectCount: Int
) {
    companion object {
        val Empty = DayCell(null, false, DayStatus.NONE, 0, 0, 0)
    }
}

data class MonthHeat(
    val month: YearMonth,
    val days: List<DayCell>
)

data class ChartPoint(
    val label: String,
    val fullLabel: String,
    val minutes: Int
)

data class StatsSummary(
    val totalMinutes: Int,
    val averageMinutesPerDay: Double,
    val dayCount: Int
)

data class DayDetailItem(
    val projectId: Long,
    val name: String,
    val completed: Boolean,
    val minutes: Int
)

data class StatsFilter(
    val anchor: LocalDate,
    val calendarMode: CalendarMode,
    val dimension: StatsDimension,
    val selectedDate: LocalDate?
)

data class StatsUiState(
    val anchor: LocalDate,
    val calendarMode: CalendarMode,
    val dimension: StatsDimension,
    val periodLabel: String,
    val monthCells: List<DayCell>,
    val yearHeat: List<MonthHeat>,
    val chartPoints: List<ChartPoint>,
    val summary: StatsSummary,
    val selectedDate: LocalDate?,
    val selectedDayDetails: List<DayDetailItem>,
    val projectCount: Int
)
