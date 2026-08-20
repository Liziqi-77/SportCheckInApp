package com.example.sportcheckin.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportcheckin.data.CheckInRecordEntity
import com.example.sportcheckin.data.ExerciseProjectEntity
import com.example.sportcheckin.data.SportRepository
import com.example.sportcheckin.model.CalendarMode
import com.example.sportcheckin.model.ChartPoint
import com.example.sportcheckin.model.DayCell
import com.example.sportcheckin.model.DayDetailItem
import com.example.sportcheckin.model.DayStatus
import com.example.sportcheckin.model.MonthHeat
import com.example.sportcheckin.model.StatsDimension
import com.example.sportcheckin.model.StatsFilter
import com.example.sportcheckin.model.StatsSummary
import com.example.sportcheckin.model.StatsUiState
import com.example.sportcheckin.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SportRepository.getInstance(application)

    private val anchor = MutableStateFlow(LocalDate.now())
    private val calendarMode = MutableStateFlow(CalendarMode.MONTH)
    private val dimension = MutableStateFlow(StatsDimension.WEEK)
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    private val recordsAroundAnchor = anchor.flatMapLatest { date ->
        repository.recordsBetween(date.minusYears(1).minusWeeks(2), date.plusYears(1).plusWeeks(2))
    }

    private val filter = combine(anchor, calendarMode, dimension, selectedDate) { a, mode, dim, selected ->
        StatsFilter(a, mode, dim, selected)
    }

    val uiState: StateFlow<StatsUiState?> = combine(
        repository.projects,
        recordsAroundAnchor,
        filter
    ) { projects, records, currentFilter ->
        buildState(projects, records, currentFilter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun setCalendarMode(mode: CalendarMode) {
        calendarMode.value = mode
    }

    fun setDimension(value: StatsDimension) {
        dimension.value = value
    }

    fun selectDate(date: LocalDate?) {
        selectedDate.value = date
    }

    fun setHistoryCompleted(item: DayDetailItem, completed: Boolean) {
        val date = selectedDate.value ?: return
        if (date.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            repository.setCheckIn(
                projectId = item.projectId,
                date = date,
                completed = completed,
                minutes = if (completed) item.minutes else 0
            )
        }
    }

    fun updateHistoryMinutes(projectId: Long, minutes: Int) {
        val date = selectedDate.value ?: return
        if (date.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            repository.setCheckIn(
                projectId = projectId,
                date = date,
                completed = true,
                minutes = minutes
            )
        }
    }

    fun goToday() {
        anchor.value = LocalDate.now()
        selectedDate.value = null
    }

    fun moveAnchor(direction: Int) {
        anchor.value = when (dimension.value) {
            StatsDimension.WEEK -> anchor.value.plusWeeks(direction.toLong())
            StatsDimension.MONTH -> anchor.value.plusMonths(direction.toLong())
            StatsDimension.QUARTER -> anchor.value.plusMonths(direction * 3L)
            StatsDimension.YEAR -> anchor.value.plusYears(direction.toLong())
        }
    }

    private fun buildState(
        projects: List<ExerciseProjectEntity>,
        records: List<CheckInRecordEntity>,
        currentFilter: StatsFilter
    ): StatsUiState {
        val completedByDate = records
            .filter { it.completed }
            .groupBy { LocalDate.parse(it.date) }

        val currentMonth = YearMonth.from(currentFilter.anchor)
        val monthCells = buildMonthCells(currentMonth, projects, completedByDate)
        val yearHeat = (1..12).map { monthValue ->
            val month = YearMonth.of(currentFilter.anchor.year, monthValue)
            MonthHeat(
                month = month,
                days = buildMonthCells(month, projects, completedByDate)
            )
        }
        val period = periodRange(currentFilter.anchor, currentFilter.dimension)
        val chartPoints = buildChartPoints(currentFilter, completedByDate)
        val summary = buildSummary(period.first, period.second, completedByDate)
        val detailDate = currentFilter.selectedDate ?: currentFilter.anchor
        val effectiveProjectsForDetail = effectiveProjectsOn(projects, detailDate)

        return StatsUiState(
            anchor = currentFilter.anchor,
            calendarMode = currentFilter.calendarMode,
            dimension = currentFilter.dimension,
            periodLabel = periodLabel(currentFilter.anchor, currentFilter.dimension, period.first, period.second),
            monthCells = monthCells,
            yearHeat = yearHeat,
            chartPoints = chartPoints,
            summary = summary,
            selectedDate = currentFilter.selectedDate,
            selectedDayDetails = buildSelectedDetails(effectiveProjectsForDetail, completedByDate, detailDate),
            projectCount = effectiveProjectsForDetail.size
        )
    }

    private fun buildMonthCells(
        month: YearMonth,
        projects: List<ExerciseProjectEntity>,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>
    ): List<DayCell> {
        val firstDay = month.atDay(1)
        val leadingBlankCount = firstDay.dayOfWeek.value - 1
        val cells = MutableList(leadingBlankCount) { DayCell.Empty }
        cells += DateUtils.daysOfMonth(month).map { date ->
            buildDayCell(date, true, effectiveProjectsOn(projects, date), completedByDate)
        }
        while (cells.size % 7 != 0) {
            cells += DayCell.Empty
        }
        return cells
    }

    private fun effectiveProjectsOn(
        projects: List<ExerciseProjectEntity>,
        date: LocalDate
    ): List<ExerciseProjectEntity> {
        val key = DateUtils.toKey(date)
        return projects.filter { it.startDate.isEmpty() || it.startDate <= key }
    }

    private fun buildDayCell(
        date: LocalDate,
        inCurrentPeriod: Boolean,
        effectiveProjects: List<ExerciseProjectEntity>,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>
    ): DayCell {
        val effectiveProjectIds = effectiveProjects.map { it.id }.toSet()
        val dayRecords = completedByDate[date].orEmpty().filter { it.projectId in effectiveProjectIds }
        val completedCount = dayRecords.count { it.completed }
        return DayCell(
            date = date,
            inCurrentPeriod = inCurrentPeriod,
            status = dayStatus(effectiveProjects.size, completedCount),
            totalMinutes = dayRecords.sumOf { it.minutes },
            completedCount = completedCount,
            projectCount = effectiveProjects.size
        )
    }

    private fun dayStatus(projectCount: Int, completedCount: Int): DayStatus {
        return when {
            projectCount == 0 -> DayStatus.NONE
            completedCount == 0 -> DayStatus.NONE
            completedCount >= projectCount -> DayStatus.ALL
            else -> DayStatus.PARTIAL
        }
    }

    private fun periodRange(anchorDate: LocalDate, value: StatsDimension): Pair<LocalDate, LocalDate> {
        return when (value) {
            StatsDimension.WEEK -> DateUtils.mondayOfWeek(anchorDate) to DateUtils.sundayOfWeek(anchorDate)
            StatsDimension.MONTH -> YearMonth.from(anchorDate).atDay(1) to YearMonth.from(anchorDate).atEndOfMonth()
            StatsDimension.QUARTER -> DateUtils.quarterStart(anchorDate) to DateUtils.quarterEnd(anchorDate)
            StatsDimension.YEAR -> LocalDate.of(anchorDate.year, 1, 1) to LocalDate.of(anchorDate.year, 12, 31)
        }
    }

    private fun periodLabel(
        anchorDate: LocalDate,
        value: StatsDimension,
        start: LocalDate,
        end: LocalDate
    ): String {
        return when (value) {
            StatsDimension.WEEK -> "${DateUtils.dayFormatter.format(start)} - ${DateUtils.dayFormatter.format(end)}"
            StatsDimension.MONTH -> DateUtils.monthLabel(YearMonth.from(anchorDate))
            StatsDimension.QUARTER -> "${anchorDate.year}年 第${DateUtils.quarterOf(anchorDate)}季度"
            StatsDimension.YEAR -> "${anchorDate.year}年"
        }
    }

    private fun buildChartPoints(
        currentFilter: StatsFilter,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>
    ): List<ChartPoint> {
        val anchorDate = currentFilter.anchor
        return when (currentFilter.dimension) {
            StatsDimension.WEEK -> {
                val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                DateUtils.daysOfWeek(anchorDate).mapIndexed { index, date ->
                    ChartPoint(
                        label = labels[index],
                        fullLabel = DateUtils.dayFormatter.format(date),
                        minutes = sumOn(date, completedByDate)
                    )
                }
            }

            StatsDimension.MONTH -> {
                DateUtils.daysOfMonth(YearMonth.from(anchorDate)).map { date ->
                    ChartPoint(
                        label = date.dayOfMonth.toString(),
                        fullLabel = DateUtils.dayFormatter.format(date),
                        minutes = sumOn(date, completedByDate)
                    )
                }
            }

            StatsDimension.QUARTER -> {
                val quarterStart = DateUtils.quarterStart(anchorDate)
                val quarterEnd = DateUtils.quarterEnd(anchorDate)
                val shortFormatter = DateTimeFormatter.ofPattern("M/d")
                DateUtils.weeksBetween(quarterStart, quarterEnd).mapIndexed { index, week ->
                    val clippedStart = maxOf(week.first, quarterStart)
                    val clippedEnd = minOf(week.second, quarterEnd)
                    ChartPoint(
                        label = "W${index + 1}",
                        fullLabel = "${clippedStart.format(shortFormatter)} ~ ${clippedEnd.format(shortFormatter)}",
                        minutes = sumBetween(clippedStart, clippedEnd, completedByDate)
                    )
                }
            }

            StatsDimension.YEAR -> {
                (1..12).map { monthValue ->
                    ChartPoint(
                        label = "${monthValue}月",
                        fullLabel = "${anchorDate.year}年${monthValue}月",
                        minutes = completedByDate
                            .filterKeys { it.year == anchorDate.year && it.monthValue == monthValue }
                            .values
                            .flatten()
                            .sumOf { it.minutes }
                    )
                }
            }
        }
    }

    private fun buildSummary(
        start: LocalDate,
        end: LocalDate,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>
    ): StatsSummary {
        val total = sumBetween(start, end, completedByDate)
        val dayCount = (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)
        return StatsSummary(
            totalMinutes = total,
            averageMinutesPerDay = total.toDouble() / dayCount,
            dayCount = dayCount
        )
    }

    private fun buildSelectedDetails(
        projects: List<ExerciseProjectEntity>,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>,
        date: LocalDate
    ): List<DayDetailItem> {
        val recordsByProject = completedByDate[date].orEmpty().associateBy { it.projectId }
        return projects.map { project ->
            val record = recordsByProject[project.id]
            DayDetailItem(
                projectId = project.id,
                name = project.name,
                completed = record?.completed == true,
                minutes = record?.minutes ?: project.targetMinutes
            )
        }
    }

    private fun sumOn(date: LocalDate, completedByDate: Map<LocalDate, List<CheckInRecordEntity>>): Int =
        completedByDate[date].orEmpty().sumOf { it.minutes }

    private fun sumBetween(
        start: LocalDate,
        end: LocalDate,
        completedByDate: Map<LocalDate, List<CheckInRecordEntity>>
    ): Int {
        return completedByDate
            .filterKeys { !it.isBefore(start) && !it.isAfter(end) }
            .values
            .flatten()
            .sumOf { it.minutes }
    }
}
