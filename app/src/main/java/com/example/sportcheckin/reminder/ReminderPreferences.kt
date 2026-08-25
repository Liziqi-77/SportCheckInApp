package com.example.sportcheckin.reminder

import android.content.Context

class ReminderPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isReminderEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, false)

    fun setReminderEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getReminderHour(): Int = preferences.getInt(KEY_HOUR, DEFAULT_HOUR)

    fun getReminderMinute(): Int = preferences.getInt(KEY_MINUTE, DEFAULT_MINUTE)

    companion object {
        private const val PREFS_NAME = "sport_check_in_settings"
        private const val KEY_ENABLED = "daily_reminder_enabled"
        private const val KEY_HOUR = "daily_reminder_hour"
        private const val KEY_MINUTE = "daily_reminder_minute"
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
    }
}
