package com.example.taskpulse.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.TaskPulseRepository
import com.example.taskpulse.data.local.TaskPulseDbHelper
import com.example.taskpulse.data.model.Habit
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.data.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

import com.example.taskpulse.data.model.HourlyCheckIn
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.data.model.SleepLog
import com.example.taskpulse.data.model.TimeClassification
import com.example.taskpulse.util.DateUtils

data class DashboardUiState(
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val activeHabitStreaks: Int = 0,
    val totalHabits: Int = 0,
    val progressFraction: Float = 0f,
    val totalCheckIns: Int = 0,
    val avgProductivityRating: Float = 0f,
    val productiveHours: Int = 0,
    val rechargeHours: Int = 0,
    val neutralHours: Int = 0,
    val wastedHours: Int = 0,
    val todaySleepHours: Float = 0f,
    val todaySleepQuality: Int = 0,
    val todaySleepNotes: String = "",
    val totalWaterMl: Float = 0f,
    val totalCaffeineMg: Int = 0,
    val coffeeCount: Int = 0,
    val energyDrinkCount: Int = 0,
    val lateCaffeineCount: Int = 0,
    val totalIntakes: Int = 0
)

class TaskPulseViewModel(
    private val repository: TaskPulseRepository = TaskPulseRepository()
) : ViewModel() {

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dbHelper = TaskPulseDbHelper(context.applicationContext)
                val repo = TaskPulseRepository(dbHelper)
                return TaskPulseViewModel(repo) as T
            }
        }
    }

    val tasks: StateFlow<List<Task>> = repository.tasks
    val habits: StateFlow<List<Habit>> = repository.habits
    val checkIns: StateFlow<List<HourlyCheckIn>> = repository.checkIns
    val sleepLogs: StateFlow<List<SleepLog>> = repository.sleepLogs
    val intakes: StateFlow<List<IntakeLog>> = repository.intakes

    val dashboardState: StateFlow<DashboardUiState> = combine(
        tasks,
        habits,
        checkIns,
        sleepLogs,
        intakes
    ) { taskList, habitList, checkInList, sleepList, intakeList ->
        val doneTasks = taskList.count { it.isCompleted }
        val totalT = taskList.size
        val activeStreaks = habitList.sumOf { it.streakCount }
        val totalH = habitList.size

        val totalItems = totalT + totalH
        val completedItems = doneTasks + habitList.count { it.isCompletedToday }
        val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f

        val todayCheckIns = checkInList.filter { DateUtils.isToday(it.dateString) }
        val effectiveCheckIns = if (todayCheckIns.isNotEmpty()) todayCheckIns else checkInList
        val avgRating = if (effectiveCheckIns.isNotEmpty()) {
            effectiveCheckIns.map { it.productivityRating }.average().toFloat()
        } else 0f

        val productiveCount = todayCheckIns.count { it.classification == TimeClassification.PRODUCTIVE }
        val rechargeCount = todayCheckIns.count { it.classification == TimeClassification.RECHARGE }
        val neutralCount = todayCheckIns.count { it.classification == TimeClassification.NEUTRAL }
        val wastedCount = todayCheckIns.count { it.classification == TimeClassification.WASTED }

        val todaySleep = sleepList.firstOrNull { DateUtils.isToday(it.dateString) } ?: sleepList.firstOrNull()

        // Filter intakes strictly for TODAY so water and caffeine counters reset each night!
        val todayIntakes = intakeList.filter { it.isActuallyToday }
        val waterMl = todayIntakes
            .filter { it.type == IntakeType.WATER }
            .sumOf { if (it.unit.equals("ml", ignoreCase = true)) it.amount.toDouble() else (it.amount * 250.0) }
            .toFloat()
        val totalCaffeine = todayIntakes.sumOf { it.caffeineMg }
        val coffee = todayIntakes.count { it.type == IntakeType.COFFEE }
        val energy = todayIntakes.count { it.type == IntakeType.ENERGY_DRINK }
        val lateCaff = todayIntakes.count { it.caffeineMg > 0 && it.hourOfDay >= 14 }

        DashboardUiState(
            completedTasks = doneTasks,
            totalTasks = totalT,
            activeHabitStreaks = activeStreaks,
            totalHabits = totalH,
            progressFraction = progress,
            totalCheckIns = todayCheckIns.size,
            avgProductivityRating = avgRating,
            productiveHours = productiveCount,
            rechargeHours = rechargeCount,
            neutralHours = neutralCount,
            wastedHours = wastedCount,
            todaySleepHours = todaySleep?.hours ?: 0f,
            todaySleepQuality = todaySleep?.qualityRating ?: 0,
            todaySleepNotes = todaySleep?.notes ?: "",
            totalWaterMl = waterMl,
            totalCaffeineMg = totalCaffeine,
            coffeeCount = coffee,
            energyDrinkCount = energy,
            lateCaffeineCount = lateCaff,
            totalIntakes = todayIntakes.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun toggleTask(taskId: String) = repository.toggleTaskCompletion(taskId)
    fun addTask(title: String, description: String, priority: Priority, category: String, dueDate: String = "Today") {
        if (title.isNotBlank()) {
            repository.addTask(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority,
                    category = category.ifBlank { "General" },
                    dueDate = dueDate.ifBlank { "Today" }
                )
            )
        }
    }
    fun deleteTask(taskId: String) = repository.deleteTask(taskId)

    fun toggleHabit(habitId: String) = repository.toggleHabitCompletion(habitId)
    fun toggleHabitDay(habitId: String, dayIndex: Int) = repository.toggleHabitDay(habitId, dayIndex)
    fun toggleHabitDate(habitId: String, dateString: String) = repository.toggleHabitForDate(habitId, dateString)

    fun addHabit(title: String, category: String, completedDates: Set<String> = emptySet(), targetDaysPerWeek: Int = 7) {
        if (title.isNotBlank()) {
            repository.addHabit(
                Habit(
                    title = title.trim(),
                    category = category.ifBlank { "Health" },
                    completedDates = completedDates,
                    targetDaysPerWeek = targetDaysPerWeek.coerceIn(1, 7)
                )
            )
        }
    }

    @JvmName("addHabitWithDays")
    fun addHabit(title: String, category: String, completedDays: Set<Int>, targetDaysPerWeek: Int = 7) {
        if (title.isNotBlank()) {
            repository.addHabit(
                Habit(
                    title = title.trim(),
                    category = category.ifBlank { "Health" },
                    completedDays = completedDays,
                    targetDaysPerWeek = targetDaysPerWeek.coerceIn(1, 7)
                )
            )
        }
    }

    fun deleteHabit(habitId: String) = repository.deleteHabit(habitId)

    fun addCheckIn(
        timeSlot: String,
        summary: String,
        productivityRating: Int,
        classification: TimeClassification,
        categoryTag: String = "Work",
        dateString: String = DateUtils.todayDateString
    ) {
        if (summary.isNotBlank()) {
            repository.addCheckIn(
                HourlyCheckIn(
                    dateString = DateUtils.normalizeDate(dateString),
                    timeSlot = timeSlot.trim(),
                    summary = summary.trim(),
                    productivityRating = productivityRating.coerceIn(1, 5),
                    classification = classification,
                    categoryTag = categoryTag.ifBlank { "Focus" }
                )
            )
        }
    }

    fun deleteCheckIn(checkInId: String) = repository.deleteCheckIn(checkInId)

    fun logSleep(
        hours: Float,
        qualityRating: Int,
        notes: String,
        bedtime: String = "11:30 PM",
        wakeTime: String = "7:00 AM",
        dateString: String = DateUtils.todayDateString
    ) {
        repository.logSleep(
            SleepLog(
                dateString = DateUtils.normalizeDate(dateString),
                hours = hours,
                qualityRating = qualityRating.coerceIn(1, 5),
                notes = notes.trim(),
                bedtime = bedtime,
                wakeTime = wakeTime
            )
        )
    }

    fun deleteSleepLog(id: String) = repository.deleteSleepLog(id)

    fun addIntake(
        type: IntakeType,
        amount: Float,
        unit: String,
        caffeineMg: Int,
        timeString: String,
        hourOfDay: Int,
        notes: String,
        dateString: String = DateUtils.todayDateString
    ) {
        repository.addIntake(
            IntakeLog(
                type = type,
                amount = amount,
                unit = unit,
                caffeineMg = caffeineMg,
                timeString = timeString.trim(),
                hourOfDay = hourOfDay,
                dateString = DateUtils.normalizeDate(dateString),
                notes = notes.trim()
            )
        )
    }

    fun quickLogDrink(type: IntakeType) {
        val now = java.util.Calendar.getInstance()
        val hour24 = now.get(java.util.Calendar.HOUR_OF_DAY)
        val timeFormatted = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(now.time)

        val (amt, unit, caff, note) = when (type) {
            IntakeType.COFFEE -> listOf(1.0f, "cup", 95, "Coffee")
            IntakeType.ENERGY_DRINK -> listOf(1.0f, "can", 160, "Energy Drink")
            IntakeType.WATER -> listOf(250f, "ml", 0, "Glass of Water")
            IntakeType.TEA -> listOf(1.0f, "cup", 40, "Tea")
            IntakeType.SODA -> listOf(1.0f, "can", 35, "Soda")
            IntakeType.OTHER -> listOf(1.0f, "serving", 0, "Beverage")
        }

        repository.addIntake(
            IntakeLog(
                type = type,
                amount = amt as Float,
                unit = unit as String,
                caffeineMg = caff as Int,
                timeString = timeFormatted,
                hourOfDay = hour24,
                dateString = DateUtils.todayDateString,
                notes = note as String
            )
        )
    }

    fun deleteIntake(id: String) = repository.deleteIntake(id)

    fun generateExportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("=== TASKPULSE DATA EXPORT ===")
        sb.appendLine("ExportedAt,${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        sb.appendLine()
        sb.appendLine("--- TASKS ---")
        sb.appendLine("ID,Title,Description,Priority,Category,DueDate,IsCompleted")
        tasks.value.forEach { t ->
            sb.appendLine("\"${t.id}\",\"${t.title.replace("\"", "\"\"")}\",\"${t.description.replace("\"", "\"\"")}\",${t.priority.name},\"${t.category}\",\"${t.dueDate}\",${t.isCompleted}")
        }
        sb.appendLine()
        sb.appendLine("--- HABITS ---")
        sb.appendLine("ID,Title,Streak,BestStreak,Category,TargetDaysPerWeek,CompletedToday")
        habits.value.forEach { h ->
            sb.appendLine("\"${h.id}\",\"${h.title.replace("\"", "\"\"")}\",${h.streakCount},${h.bestStreak},\"${h.category}\",${h.targetDaysPerWeek},${h.isCompletedToday}")
        }
        sb.appendLine()
        sb.appendLine("--- HOURLY CHECK-INS ---")
        sb.appendLine("ID,TimeSlot,Summary,Rating,Classification,Tag")
        checkIns.value.forEach { c ->
            sb.appendLine("\"${c.id}\",\"${c.timeSlot}\",\"${c.summary.replace("\"", "\"\"")}\",${c.productivityRating},${c.classification.name},\"${c.categoryTag}\"")
        }
        sb.appendLine()
        sb.appendLine("--- SLEEP LOGS ---")
        sb.appendLine("ID,Date,Hours,QualityRating,Bedtime,WakeTime,Notes")
        sleepLogs.value.forEach { s ->
            sb.appendLine("\"${s.id}\",\"${s.dateString}\",${s.hours},${s.qualityRating},\"${s.bedtime}\",\"${s.wakeTime}\",\"${s.notes.replace("\"", "\"\"")}\"")
        }
        sb.appendLine()
        sb.appendLine("--- INTAKE LOGS ---")
        sb.appendLine("ID,Date,Type,Amount,Unit,CaffeineMg,TimeLogged,HourOfDay,Notes")
        intakes.value.forEach { i ->
            sb.appendLine("\"${i.id}\",\"${i.dateString}\",${i.type.name},${i.amount},\"${i.unit}\",${i.caffeineMg},\"${i.timeString}\",${i.hourOfDay},\"${i.notes.replace("\"", "\"\"")}\"")
        }
        return sb.toString()
    }
}
