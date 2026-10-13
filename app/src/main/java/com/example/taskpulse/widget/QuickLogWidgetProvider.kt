package com.example.taskpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.taskpulse.MainActivity
import com.example.taskpulse.R
import com.example.taskpulse.data.local.TaskPulseDbHelper
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.notifications.ReminderManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class QuickLogWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_LOG_COFFEE = "com.example.taskpulse.ACTION_LOG_COFFEE"
        const val ACTION_LOG_ENERGY = "com.example.taskpulse.ACTION_LOG_ENERGY"
        const val ACTION_LOG_WATER = "com.example.taskpulse.ACTION_LOG_WATER"
        const val ACTION_PIN_WIDGET = "com.example.taskpulse.ACTION_PIN_WIDGET"

        fun requestPinWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, QuickLogWidgetProvider::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    200,
                    Intent(context, QuickLogWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            } else {
                Toast.makeText(context, "Pinning widgets is not supported by your launcher. Please add manually from your Home Screen widgets menu.", Toast.LENGTH_LONG).show()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, QuickLogWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_log)

            // Read counts from database
            val db = TaskPulseDbHelper(context)
            val intakes = db.getAllIntakeLogs()
            val todayIntakes = intakes.filter { it.isActuallyToday }
            val coffeeCount = todayIntakes.count { it.type == IntakeType.COFFEE }
            val energyCount = todayIntakes.count { it.type == IntakeType.ENERGY_DRINK }
            val waterMl = todayIntakes
                .filter { it.type == IntakeType.WATER }
                .sumOf { if (it.unit.equals("ml", ignoreCase = true)) it.amount.toDouble() else (it.amount * 250.0) }
                .toInt()

            views.setTextViewText(
                R.id.widget_status_text,
                "Today: ☕ $coffeeCount • ⚡ $energyCount • 💧 ${waterMl}ml"
            )

            // Launch App intent
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                100,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_open_app, appPendingIntent)

            // Quick Log Coffee
            val coffeeIntent = Intent(context, QuickLogWidgetProvider::class.java).apply {
                action = ACTION_LOG_COFFEE
            }
            val coffeePendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                coffeeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_coffee, coffeePendingIntent)

            // Quick Log Energy Drink
            val energyIntent = Intent(context, QuickLogWidgetProvider::class.java).apply {
                action = ACTION_LOG_ENERGY
            }
            val energyPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                energyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_energy, energyPendingIntent)

            // Quick Log Water
            val waterIntent = Intent(context, QuickLogWidgetProvider::class.java).apply {
                action = ACTION_LOG_WATER
            }
            val waterPendingIntent = PendingIntent.getBroadcast(
                context,
                103,
                waterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_water, waterPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        val action = intent?.action ?: return

        val now = Calendar.getInstance()
        val hour24 = now.get(Calendar.HOUR_OF_DAY)
        val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(now.time)
        val db = TaskPulseDbHelper(context)

        when (action) {
            ACTION_LOG_COFFEE -> {
                db.insertIntakeLog(
                    IntakeLog(
                        type = IntakeType.COFFEE,
                        amount = 1.0f,
                        unit = "cup",
                        caffeineMg = 95,
                        timeString = timeFormatted,
                        hourOfDay = hour24,
                        notes = "Quick logged via Home Widget"
                    )
                )
                if (hour24 >= 14) {
                    val remainingAtBedtime = (95 * Math.pow(0.5, ((23.5 - hour24) / 5.5))).toInt().coerceAtLeast(1)
                    ReminderManager.scheduleWindDownReminder(context, "Coffee", 95, hour24, remainingAtBedtime)
                }
                Toast.makeText(context, "☕ Logged Coffee (+95mg) via Widget!", Toast.LENGTH_SHORT).show()
                updateAllWidgets(context)
            }
            ACTION_LOG_ENERGY -> {
                db.insertIntakeLog(
                    IntakeLog(
                        type = IntakeType.ENERGY_DRINK,
                        amount = 1.0f,
                        unit = "can",
                        caffeineMg = 160,
                        timeString = timeFormatted,
                        hourOfDay = hour24,
                        notes = "Quick logged via Home Widget"
                    )
                )
                if (hour24 >= 14) {
                    val remainingAtBedtime = (160 * Math.pow(0.5, ((23.5 - hour24) / 5.5))).toInt().coerceAtLeast(1)
                    ReminderManager.scheduleWindDownReminder(context, "Energy Drink", 160, hour24, remainingAtBedtime)
                }
                Toast.makeText(context, "⚡ Logged Energy Drink (+160mg) via Widget!", Toast.LENGTH_SHORT).show()
                updateAllWidgets(context)
            }
            ACTION_LOG_WATER -> {
                db.insertIntakeLog(
                    IntakeLog(
                        type = IntakeType.WATER,
                        amount = 250f,
                        unit = "ml",
                        caffeineMg = 0,
                        timeString = timeFormatted,
                        hourOfDay = hour24,
                        notes = "Quick logged via Home Widget"
                    )
                )
                Toast.makeText(context, "💧 Logged 250ml Water via Widget!", Toast.LENGTH_SHORT).show()
                updateAllWidgets(context)
            }
            ACTION_PIN_WIDGET -> {
                requestPinWidget(context)
            }
        }
    }
}
