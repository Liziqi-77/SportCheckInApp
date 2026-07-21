package com.example.sportcheckin.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

object DateUtils {
    val storageFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)

    fun today(): LocalDate = LocalDate.now()

    fun toKey(date: LocalDate): String = date.format(storageFormatter)

    fun mondayOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun sundayOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    fun daysOfWeek(date: LocalDate): List<LocalDate> {
        val monday = mondayOfWeek(date)
        return (0L..6L).map(monday::plusDays)
    }

    fun daysOfMonth(month: YearMonth): List<LocalDate> =
        (1..month.lengthOfMonth()).map(month::atDay)

    fun quarterOf(date: LocalDate): Int = (date.monthValue - 1) / 3 + 1

    fun quarterStart(date: LocalDate): LocalDate =
        LocalDate.of(date.year, ((date.monthValue - 1) / 3) * 3 + 1, 1)

    fun quarterEnd(date: LocalDate): LocalDate = quarterStart(date).plusMonths(3).minusDays(1)

    fun weeksBetween(start: LocalDate, end: LocalDate): List<Pair<LocalDate, LocalDate>> {
        val result = mutableListOf<Pair<LocalDate, LocalDate>>()
        var cursor = mondayOfWeek(start)
        val last = sundayOfWeek(end)
        while (!cursor.isAfter(last)) {
            val weekEnd = cursor.plusDays(6)
            result += cursor to weekEnd
            cursor = cursor.plusWeeks(1)
        }
        return result
    }

    fun isoWeekLabel(date: LocalDate): String {
        val weekFields = WeekFields.of(Locale.CHINA)
        val week = date.get(weekFields.weekOfWeekBasedYear())
        return "第${week}周"
    }

    fun monthLabel(month: YearMonth): String = "${month.year}年${month.monthValue}月"
}

fun Int.toDurationText(): String {
    return if (this < 60) {
        "${this}分钟"
    } else {
        val hour = this / 60
        val minute = this % 60
        if (minute == 0) "${hour}小时" else "${hour}小时${minute}分钟"
    }
}
