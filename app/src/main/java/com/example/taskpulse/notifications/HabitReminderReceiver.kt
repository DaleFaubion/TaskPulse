package com.example.taskpulse.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.taskpulse.MainActivity
import java.util.Calendar

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "taskpulse_habit_reminders"
        const val CHANNEL_NAME = "Habit & Task Reminders"
        const val CHANNEL_WIND_DOWN_ID = "taskpulse_wind_down"
        const val CHANNEL_WIND_DOWN_NAME = "Evening Wind-Down & Sleep"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_WIND_DOWN_ID = 1002
        const val EXTRA_OPEN_CHECKIN = "extra_open_checkin"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val habitChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders to check in habits and complete pending tasks"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(habitChannel)

                val windDownChannel = NotificationChannel(
                    CHANNEL_WIND_DOWN_ID,
                    CHANNEL_WIND_DOWN_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Smart bedtime wind-down alerts when late stimulants or caffeine are detected"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(windDownChannel)
            }
        }

        fun showWindDownNotification(
            context: Context,
            title: String = "🌙 Evening Wind-Down • TaskPulse",
            message: String = "Late caffeine detected. Start dimming screens and wind down early to protect deep sleep tonight."
        ) {
            ensureNotificationChannel(context)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                201,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_WIND_DOWN_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_today,
                    "Sleep Log 🌙",
                    pendingIntent
                )
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_WIND_DOWN_ID, notification)
        }

        fun showNotification(
            context: Context,
            title: String = "TaskPulse • Streak Reminder 🔥",
            message: String = "Keep your momentum going! Check in your habits and power through today's goals."
        ) {
            ensureNotificationChannel(context)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val hourlyIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_CHECKIN, true)
            }
            val hourlyPendingIntent = PendingIntent.getActivity(
                context,
                101,
                hourlyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_today,
                    "Habits ✓",
                    pendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_recent_history,
                    "Log Last Hour ⏱️",
                    hourlyPendingIntent
                )
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val config = ReminderManager.getReminderConfig(context)

        if (intent?.action == ReminderManager.ACTION_WIND_DOWN) {
            val title = intent.getStringExtra("title") ?: "🌙 Evening Wind-Down • TaskPulse"
            val message = intent.getStringExtra("message") ?: "Late caffeine detected. Start winding down early to protect deep sleep."
            showWindDownNotification(context, title, message)
            return
        }

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (config.isEnabled) {
                ReminderManager.scheduleReminder(context, config)
            }
            return
        }

        if (!config.isEnabled) return

        // Check quiet hours if enabled
        val shouldNotify = if (config.quietHoursEnabled) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (config.startHour <= config.endHour) {
                currentHour in config.startHour until config.endHour
            } else {
                currentHour >= config.startHour || currentHour < config.endHour
            }
        } else {
            true
        }

        if (shouldNotify) {
            showNotification(context, config.notificationTitle, config.notificationMessage)
        }

        // Reschedule next interval
        ReminderManager.scheduleReminder(context, config)
    }
}
