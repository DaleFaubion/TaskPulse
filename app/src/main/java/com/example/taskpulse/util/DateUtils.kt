package com.example.taskpulse.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayInfo(
    val dateString: String,    // "yyyy-MM-dd"
    val dayLabel: String,      // "M", "T", "W"...
    val dayShortName: String,  // "Mon", "Tue"...
    val dayNumber: String,     // "12", "11"...
    val isToday: Boolean,
    val isFuture: Boolean = false
)

object DateUtils {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val dayNamesShort = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayNamesFull = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val shortDisplayFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val todayDateString: String
        get() = isoDateFormat.format(Date())

    val yesterdayDateString: String
        get() = getDateWithOffset(-1)

    /**
     * Returns 0 for Monday, 1 for Tuesday, ..., 6 for Sunday (ISO-8601 standard week index).
     */
    val currentDayOfWeekIndex: Int
        get() {
            val cal = Calendar.getInstance()
            return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        }

    fun isToday(dateStr: String?, timestamp: Long = 0L): Boolean {
        if (dateStr.isNullOrBlank()) return false
        if (dateStr == todayDateString) return true
        if (dateStr.equals("Today", ignoreCase = true)) {
            return if (timestamp > 0L) formatTimestampToDate(timestamp) == todayDateString else false
        }
        return false
    }

    fun isYesterday(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank()) return false
        if (dateStr.equals("Yesterday", ignoreCase = true)) return true
        return dateStr == yesterdayDateString
    }

    fun isToday(dayIndex: Int): Boolean = dayIndex == currentDayOfWeekIndex

    fun getDayLabel(dayIndex: Int): String = dayLabels.getOrElse(dayIndex) { "M" }

    fun getDayShortName(dayIndex: Int): String = dayNamesShort.getOrElse(dayIndex) { "Mon" }

    fun getDateWithOffset(dayOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        return isoDateFormat.format(cal.time)
    }

    fun normalizeDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return todayDateString
        if (dateStr.equals("Today", ignoreCase = true)) return todayDateString
        if (dateStr.equals("Yesterday", ignoreCase = true)) return yesterdayDateString
        if (dateStr.equals("2 Days Ago", ignoreCase = true)) return getDateWithOffset(-2)
        if (dateStr.equals("3 Days Ago", ignoreCase = true)) return getDateWithOffset(-3)
        return dateStr
    }

    fun formatDisplayDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "Today"
        if (dateStr.equals("Today", ignoreCase = true) || dateStr == todayDateString) {
            return "Today"
        }
        if (dateStr.equals("Yesterday", ignoreCase = true) || dateStr == yesterdayDateString) {
            return "Yesterday"
        }
        return try {
            val date = isoDateFormat.parse(dateStr)
            if (date != null) shortDisplayFormat.format(date) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatFullDisplayDate(dateStr: String?): String {
        val normalized = normalizeDate(dateStr)
        if (normalized == todayDateString) {
            return "Today, ${displayFormat.format(Date())}"
        }
        if (normalized == yesterdayDateString) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            return "Yesterday, ${displayFormat.format(cal.time)}"
        }
        return try {
            val date = isoDateFormat.parse(normalized)
            if (date != null) displayFormat.format(date) else normalized
        } catch (_: Exception) {
            normalized
        }
    }

    fun formatTimestampToDate(timestamp: Long): String {
        return isoDateFormat.format(Date(timestamp))
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    val todayHeaderString: String
        get() {
            val cal = Calendar.getInstance()
            val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayName = dayNamesFull.getOrElse(dow) { "Today" }
            val monthFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
            return "$dayName, ${monthFormat.format(cal.time)}"
        }

    /**
     * Returns the 7 days of the current calendar week (Monday through Sunday).
     * The day matching today has `isToday = true`.
     */
    fun getCurrentWeekDates(): List<DayInfo> {
        val list = mutableListOf<DayInfo>()
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        val daysFromMonday = (dayOfWeek + 5) % 7 // Monday=0, Tuesday=1, ..., Sunday=6

        val mondayCal = Calendar.getInstance()
        mondayCal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val todayStr = todayDateString

        for (i in 0..6) {
            val c = Calendar.getInstance()
            c.time = mondayCal.time
            c.add(Calendar.DAY_OF_YEAR, i)
            val dStr = isoDateFormat.format(c.time)
            val dow = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayLabel = dayLabels.getOrElse(dow) { "M" }
            val dayShortName = dayNamesShort.getOrElse(dow) { "Mon" }
            val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
            val isToday = dStr == todayStr
            val isFuture = dStr > todayStr

            list.add(
                DayInfo(
                    dateString = dStr,
                    dayLabel = dayLabel,
                    dayShortName = dayShortName,
                    dayNumber = dayNum,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )
        }
        return list
    }

    /**
     * Returns a scrollable range of days centered around Today (e.g. 14 days back, 3 days ahead).
     */
    fun getScrollableDayRange(pastDays: Int = 14, futureDays: Int = 3): List<DayInfo> {
        val list = mutableListOf<DayInfo>()
        val todayStr = todayDateString

        for (offset in -pastDays..futureDays) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, offset)
            val dStr = isoDateFormat.format(c.time)
            val dow = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayLabel = dayLabels.getOrElse(dow) { "M" }
            val dayShortName = dayNamesShort.getOrElse(dow) { "Mon" }
            val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
            val isToday = dStr == todayStr
            val isFuture = dStr > todayStr

            list.add(
                DayInfo(
                    dateString = dStr,
                    dayLabel = dayLabel,
                    dayShortName = dayShortName,
                    dayNumber = dayNum,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )
        }
        return list
    }

    fun getRecentDayOptions(): List<Pair<String, String>> {
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val day2Cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
        val day3Cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }

        fun labelFor(c: Calendar, prefix: String? = null): String {
            val dow = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayShort = dayNamesShort.getOrElse(dow) { "" }
            val dayNum = c.get(Calendar.DAY_OF_MONTH)
            return if (prefix != null) "$prefix ($dayShort $dayNum)" else "$dayShort $dayNum"
        }

        return listOf(
            todayDateString to labelFor(todayCal, "Today"),
            yesterdayDateString to labelFor(yesterdayCal, "Yesterday"),
            getDateWithOffset(-2) to labelFor(day2Cal),
            getDateWithOffset(-3) to labelFor(day3Cal)
        )
    }

    /**
     * Returns the past [count] days ending with today.
     * Earliest first, today last.
     */
    fun getPastDays(count: Int = 7): List<DayInfo> {
        val list = mutableListOf<DayInfo>()
        val cal = Calendar.getInstance()
        val todayStr = todayDateString

        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = isoDateFormat.format(c.time)
            val dayOfWeek = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayLabel = dayLabels.getOrElse(dayOfWeek) { "M" }
            val dayShortName = dayNamesShort.getOrElse(dayOfWeek) { "Mon" }
            val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
            val isToday = dStr == todayStr

            list.add(
                DayInfo(
                    dateString = dStr,
                    dayLabel = dayLabel,
                    dayShortName = dayShortName,
                    dayNumber = dayNum,
                    isToday = isToday
                )
            )
        }
        return list
    }

    /**
     * Calculates consecutive daily streak count for completed dates up to today.
     */
    fun calculateStreak(completedDates: Set<String>): Int {
        val todayStr = todayDateString
        val yesterdayStr = yesterdayDateString
        val todayDone = completedDates.contains(todayStr)
        val yesterdayDone = completedDates.contains(yesterdayStr)

        if (!todayDone && !yesterdayDone) {
            return 0
        }

        var streak = if (todayDone) 1 else 0
        val cal = Calendar.getInstance()
        if (todayDone) {
            cal.add(Calendar.DAY_OF_YEAR, -1) // check from yesterday back
        } else {
            // today not done, but yesterday is done
            streak = 1
            cal.add(Calendar.DAY_OF_YEAR, -2) // check from 2 days ago back
        }

        while (true) {
            val checkStr = isoDateFormat.format(cal.time)
            if (completedDates.contains(checkStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}

