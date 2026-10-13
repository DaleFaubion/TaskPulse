package com.example.taskpulse.data.model

import com.example.taskpulse.util.DateUtils
import java.util.UUID

enum class IntakeType(
    val displayName: String,
    val icon: String,
    val defaultCaffeineMg: Int,
    val defaultUnit: String
) {
    WATER("Water", "💧", 0, "ml"),
    COFFEE("Coffee", "☕", 95, "cup"),
    ENERGY_DRINK("Energy Drink", "⚡", 160, "can"),
    TEA("Tea", "🍵", 40, "cup"),
    SODA("Soda", "🥤", 35, "can"),
    OTHER("Other", "🍹", 0, "serving");

    companion object {
        fun fromString(value: String): IntakeType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: WATER
        }
    }
}

data class IntakeLog(
    val id: String = UUID.randomUUID().toString(),
    val type: IntakeType = IntakeType.WATER,
    val amount: Float = 1.0f,
    val unit: String = "cup",
    val caffeineMg: Int = 0,
    val timeString: String = "10:00 AM",
    val hourOfDay: Int = 10, // 0..23
    val dateString: String = DateUtils.todayDateString,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val isActuallyToday: Boolean
        get() {
            if (dateString.isNotBlank() && !dateString.equals("Today", ignoreCase = true)) {
                return dateString == DateUtils.todayDateString
            }
            if (timestamp > 0L) {
                return DateUtils.formatTimestampToDate(timestamp) == DateUtils.todayDateString
            }
            return false
        }
}
