package com.example.sportcheckin

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sportcheckin.reminder.ReminderScheduler
import com.example.sportcheckin.workers.DailyResetWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class SportCheckInApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailyMaintenance()
        ReminderScheduler.syncWithPreference(this)
    }

    private fun scheduleDailyMaintenance() {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val initialDelay = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(60_000L)
        val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailyResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
