package com.example.taskpulse.notifications

data class ReminderConfig(
    val isEnabled: Boolean = true,
    val intervalHours: Int = 2, // 1 = every hour, 2 = every 2 hours, 3, 4, 6, 8, 12, 24 = daily
    val startHour: Int = 8,     // Active window start (e.g. 8 AM)
    val endHour: Int = 22,      // Active window end (e.g. 10 PM)
    val quietHoursEnabled: Boolean = true,
    val notificationTitle: String = "TaskPulse • Streak Reminder 🔥",
    val notificationMessage: String = "Keep your momentum going! Check in your habits and power through today's goals."
)
