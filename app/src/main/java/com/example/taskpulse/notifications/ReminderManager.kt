package com.example.taskpulse.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderManager {

    private const val PREFS_NAME = "taskpulse_reminders_prefs"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_INTERVAL = "interval_hours"
    private const val KEY_START_HOUR = "start_hour"
    private const val KEY_END_HOUR = "end_hour"
    private const val KEY_QUIET_HOURS = "quiet_hours_enabled"

    const val REMINDER_REQUEST_CODE = 2001
    const val ACTION_REMINDER = "com.example.taskpulse.ACTION_REMINDER"

    fun getReminderConfig(context: Context): ReminderConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ReminderConfig(
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            intervalHours = prefs.getInt(KEY_INTERVAL, 2),
            startHour = prefs.getInt(KEY_START_HOUR, 8),
            endHour = prefs.getInt(KEY_END_HOUR, 22),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_HOURS, true)
        )
    }

    fun saveReminderConfig(context: Context, config: ReminderConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putInt(KEY_INTERVAL, config.intervalHours)
            .putInt(KEY_START_HOUR, config.startHour)
            .putInt(KEY_END_HOUR, config.endHour)
            .putBoolean(KEY_QUIET_HOURS, config.quietHoursEnabled)
            .apply()

        if (config.isEnabled) {
            scheduleReminder(context, config)
        } else {
            cancelReminder(context)
        }
    }

    fun scheduleReminder(context: Context, config: ReminderConfig = getReminderConfig(context)) {
        if (!config.isEnabled) return

        HabitReminderReceiver.ensureNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = config.intervalHours.toLong() * 60L * 60L * 1000L
        val nextTriggerTime = System.currentTimeMillis() + intervalMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Android 12+ fallback
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } catch (_: Exception) {}
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    const val ACTION_WIND_DOWN = "com.example.taskpulse.ACTION_WIND_DOWN"
    const val WIND_DOWN_REQUEST_CODE = 2002

    fun scheduleWindDownReminder(
        context: Context,
        beverageName: String,
        caffeineMg: Int,
        hourLogged: Int,
        activeMgAtBedtime: Int
    ) {
        HabitReminderReceiver.ensureNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val title = "🌙 Evening Wind-Down • TaskPulse"
        val message = "Late $beverageName logged ($caffeineMg mg). ~$activeMgAtBedtime mg caffeine still active at bedtime. Dim screens & start wind-down early to protect deep sleep!"

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_WIND_DOWN
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WIND_DOWN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule for 9:30 PM (21:30) today, or in 1 hour if it is already past 9:30 PM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var triggerMillis = calendar.timeInMillis
        if (triggerMillis <= System.currentTimeMillis()) {
            // If already past 9:30 PM, notify in 30 minutes to catch wind-down before midnight
            triggerMillis = System.currentTimeMillis() + (30 * 60 * 1000L)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    fun triggerWindDownTest(context: Context) {
        HabitReminderReceiver.showWindDownNotification(
            context,
            title = "🌙 Evening Wind-Down • TaskPulse",
            message = "Late Energy Drink logged (160mg). ~72mg caffeine still active at 11:30 PM bedtime. Dim screens early to protect deep sleep!"
        )
    }

    fun triggerTestNotification(
        context: Context,
        title: String = "TaskPulse • Streak Reminder 🔥",
        message: String = "Test notification! Stay on track, check in habits, and hit your goals today!"
    ) {
        HabitReminderReceiver.showNotification(context, title, message)
    }

    fun getSummaryText(config: ReminderConfig): String {
        if (!config.isEnabled) return "Reminders Off"
        return when (config.intervalHours) {
            1 -> "Every 1 hr"
            2 -> "Every 2 hrs"
            3 -> "Every 3 hrs"
            4 -> "Every 4 hrs"
            6 -> "Every 6 hrs"
            8 -> "Every 8 hrs"
            12 -> "Every 12 hrs"
            24 -> "Daily (24h)"
            else -> "Every ${config.intervalHours} hrs"
        }
    }

    fun getShortSummaryText(config: ReminderConfig): String {
        if (!config.isEnabled) return "Off"
        return "${config.intervalHours} hr"
    }
}
