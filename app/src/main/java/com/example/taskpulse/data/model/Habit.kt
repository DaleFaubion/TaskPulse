package com.example.taskpulse.data.model

import com.example.taskpulse.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val category: String = "Health",
    val completedDates: Set<String> = emptySet(), // Set of "yyyy-MM-dd"
    val targetDaysPerWeek: Int = 7
) {
    val isCompletedToday: Boolean
        get() = completedDates.contains(DateUtils.todayDateString)

    val completedDays: Set<Int>
        get() = completedDates.mapNotNull { dateStr ->
            try {
                val cal = Calendar.getInstance()
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                if (parsed != null) {
                    cal.time = parsed
                    (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                } else null
            } catch (_: Exception) {
                null
            }
        }.toSet()

    // Secondary constructor for backwards compatibility with tests and callers
    constructor(
        id: String = UUID.randomUUID().toString(),
        title: String,
        streakCount: Int = 0,
        bestStreak: Int = 0,
        category: String = "Health",
        isCompletedToday: Boolean = false,
        completedDays: Set<Int> = emptySet(),
        targetDaysPerWeek: Int = 7
    ) : this(
        id = id,
        title = title,
        streakCount = streakCount,
        bestStreak = bestStreak,
        category = category,
        completedDates = convertLegacyCompletedDays(completedDays, isCompletedToday),
        targetDaysPerWeek = targetDaysPerWeek
    )

    companion object {
        fun convertLegacyCompletedDays(completedDays: Set<Int>, isCompletedToday: Boolean): Set<String> {
            val dates = mutableSetOf<String>()
            val todayIdx = DateUtils.currentDayOfWeekIndex
            completedDays.forEach { dayIdx ->
                if (dayIdx != todayIdx || isCompletedToday) {
                    val diff = dayIdx - todayIdx
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, diff)
                    dates.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time))
                }
            }
            if (isCompletedToday) {
                dates.add(DateUtils.todayDateString)
            }
            return dates
        }
    }
}

