package com.example.sportcheckin.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val WORK_NAME = "sport_check_in_daily_reminder"
    const val CHANNEL_ID = "daily_check_in_reminder"

    fun syncWithPreference(context: Context) {
        val preferences = ReminderPreferences(context)
        if (preferences.isReminderEnabled()) {
            schedule(context, preferences.getReminderHour(), preferences.getReminderMinute())
        } else {
            cancel(context)
        }
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var nextRun = now.toLocalDate().atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun).toMillis().coerceAtLeast(60_000L)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
