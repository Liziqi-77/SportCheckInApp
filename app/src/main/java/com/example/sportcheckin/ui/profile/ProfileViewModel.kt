package com.example.sportcheckin.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sportcheckin.reminder.ReminderPreferences
import com.example.sportcheckin.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = ReminderPreferences(application)
    private val app = application
    private var pendingEnableAfterPermission = false

    private val _reminderEnabled = MutableStateFlow(preferences.isReminderEnabled())
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled

    fun onReminderSwitchChanged(
        enabled: Boolean,
        hasNotificationPermission: Boolean,
        requestPermission: () -> Unit
    ) {
        if (!enabled) {
            setReminderEnabled(false)
            return
        }
        if (hasNotificationPermission) {
            setReminderEnabled(true)
        } else {
            pendingEnableAfterPermission = true
            requestPermission()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted && pendingEnableAfterPermission) {
            setReminderEnabled(true)
        }
        pendingEnableAfterPermission = false
    }

    private fun setReminderEnabled(enabled: Boolean) {
        preferences.setReminderEnabled(enabled)
        _reminderEnabled.value = enabled
        if (enabled) {
            ReminderScheduler.schedule(
                app,
                preferences.getReminderHour(),
                preferences.getReminderMinute()
            )
        } else {
            ReminderScheduler.cancel(app)
        }
    }
}
