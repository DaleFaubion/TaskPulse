package com.example.taskpulse.data

import com.example.taskpulse.data.local.TaskPulseDbHelper
import com.example.taskpulse.data.model.Habit
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.data.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.taskpulse.util.DateUtils

import com.example.taskpulse.data.model.HourlyCheckIn
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.data.model.SleepLog
import com.example.taskpulse.data.model.TimeClassification

class TaskPulseRepository(
    private val dbHelper: TaskPulseDbHelper? = null
) {

    private val _tasks = MutableStateFlow<List<Task>>(
        dbHelper?.getAllTasks() ?: listOf(
            Task(
                title = "Design App UI in Jetpack Compose",
                description = "Build frosted glass cards and vibrant accent components",
                priority = Priority.HIGH,
                category = "Development",
                dueDate = "Today",
                isCompleted = true
            ),
            Task(
                title = "Setup Local Persistence Layer",
                description = "On-device SQLite database configured for offline reliability",
                priority = Priority.HIGH,
                category = "Architecture",
                dueDate = "Today",
                isCompleted = true
            ),
            Task(
                title = "Review Play Store Asset Requirements",
                description = "Prepare 512x512 icon, feature graphic, and screenshots",
                priority = Priority.MEDIUM,
                category = "Publishing",
                dueDate = "Tomorrow",
                isCompleted = false
            ),
            Task(
                title = "Configure Daily Habit Reminders",
                description = "Schedule notification alerts for daily streaks",
                priority = Priority.LOW,
                category = "Notifications",
                dueDate = "This Week",
                isCompleted = false
            )
        )
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(
        dbHelper?.getAllHabits() ?: run {
            val yesterday = DateUtils.getDateWithOffset(-1)
            val twoDaysAgo = DateUtils.getDateWithOffset(-2)
            val threeDaysAgo = DateUtils.getDateWithOffset(-3)
            listOf(
                Habit(
                    title = "Morning Meditation",
                    streakCount = 2,
                    bestStreak = 14,
                    category = "Mindfulness",
                    completedDates = setOf(yesterday, twoDaysAgo)
                ),
                Habit(
                    title = "Drink 2.5L Water",
                    streakCount = 3,
                    bestStreak = 12,
                    category = "Health",
                    completedDates = setOf(yesterday, twoDaysAgo, threeDaysAgo)
                ),
                Habit(
                    title = "Code Android 45 Mins",
                    streakCount = 1,
                    bestStreak = 9,
                    category = "Learning",
                    completedDates = setOf(yesterday)
                ),
                Habit(
                    title = "Evening Reading",
                    streakCount = 0,
                    bestStreak = 10,
                    category = "Growth",
                    completedDates = setOf(twoDaysAgo)
                )
            )
        }
    )
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _checkIns = MutableStateFlow<List<HourlyCheckIn>>(
        dbHelper?.getAllCheckIns() ?: listOf(
            HourlyCheckIn(
                dateString = DateUtils.todayDateString,
                timeSlot = "9:00 AM - 10:00 AM",
                summary = "Team standup & reviewed backlog sprint goals",
                productivityRating = 4,
                classification = TimeClassification.PRODUCTIVE,
                categoryTag = "Planning"
            ),
            HourlyCheckIn(
                dateString = DateUtils.todayDateString,
                timeSlot = "10:00 AM - 11:00 AM",
                summary = "Deep focus: Implemented core architecture & persistence logic",
                productivityRating = 5,
                classification = TimeClassification.PRODUCTIVE,
                categoryTag = "Coding"
            ),
            HourlyCheckIn(
                dateString = DateUtils.todayDateString,
                timeSlot = "11:00 AM - 12:00 PM",
                summary = "Coffee break, walked outside & stretched",
                productivityRating = 4,
                classification = TimeClassification.RECHARGE,
                categoryTag = "Break"
            )
        )
    )
    val checkIns: StateFlow<List<HourlyCheckIn>> = _checkIns.asStateFlow()

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) {
                    val updated = task.copy(isCompleted = !task.isCompleted)
                    dbHelper?.updateTask(updated)
                    updated
                } else task
            }
        }
    }

    fun addTask(task: Task) {
        dbHelper?.insertTask(task)
        _tasks.update { current -> listOf(task) + current }
    }

    fun deleteTask(taskId: String) {
        dbHelper?.deleteTask(taskId)
        _tasks.update { current -> current.filterNot { it.id == taskId } }
    }

    fun toggleHabitCompletion(habitId: String) {
        toggleHabitForDate(habitId, DateUtils.todayDateString)
    }

    fun toggleHabitForDate(habitId: String, dateString: String) {
        val targetDate = DateUtils.normalizeDate(dateString)
        _habits.update { current ->
            current.map { habit ->
                if (habit.id == habitId) {
                    val nextDates = if (targetDate in habit.completedDates) {
                        habit.completedDates - targetDate
                    } else {
                        habit.completedDates + targetDate
                    }
                    val nextStreak = DateUtils.calculateStreak(nextDates)
                    val nextBest = maxOf(nextStreak, habit.bestStreak)
                    val updated = habit.copy(
                        completedDates = nextDates,
                        streakCount = nextStreak,
                        bestStreak = nextBest
                    )
                    dbHelper?.updateHabit(updated)
                    updated
                } else habit
            }
        }
    }

    fun toggleHabitDay(habitId: String, dayIndex: Int) {
        val todayIdx = DateUtils.currentDayOfWeekIndex
        val diff = dayIndex - todayIdx
        val targetDate = DateUtils.getDateWithOffset(diff)
        toggleHabitForDate(habitId, targetDate)
    }

    fun addHabit(habit: Habit) {
        val calculatedStreak = DateUtils.calculateStreak(habit.completedDates)
        val finalHabit = habit.copy(
            streakCount = maxOf(habit.streakCount, calculatedStreak),
            bestStreak = maxOf(habit.bestStreak, calculatedStreak)
        )
        dbHelper?.insertHabit(finalHabit)
        _habits.update { current -> listOf(finalHabit) + current }
    }

    fun deleteHabit(habitId: String) {
        dbHelper?.deleteHabit(habitId)
        _habits.update { current -> current.filterNot { it.id == habitId } }
    }

    fun addCheckIn(checkIn: HourlyCheckIn) {
        dbHelper?.insertCheckIn(checkIn)
        _checkIns.update { current -> listOf(checkIn) + current }
    }

    fun deleteCheckIn(checkInId: String) {
        dbHelper?.deleteCheckIn(checkInId)
        _checkIns.update { current -> current.filterNot { it.id == checkInId } }
    }

    private val _sleepLogs = MutableStateFlow<List<SleepLog>>(
        dbHelper?.getAllSleepLogs()?.ifEmpty { null } ?: listOf(
            SleepLog(
                dateString = "Today",
                hours = 7.5f,
                qualityRating = 4,
                notes = "Deep, restorative sleep",
                bedtime = "11:30 PM",
                wakeTime = "7:00 AM"
            )
        )
    )
    val sleepLogs: StateFlow<List<SleepLog>> = _sleepLogs.asStateFlow()

    fun logSleep(sleepLog: SleepLog) {
        dbHelper?.insertSleepLog(sleepLog)
        _sleepLogs.update { current ->
            val filtered = current.filterNot { it.dateString == sleepLog.dateString }
            listOf(sleepLog) + filtered
        }
    }

    fun deleteSleepLog(id: String) {
        dbHelper?.deleteSleepLog(id)
        _sleepLogs.update { current -> current.filterNot { it.id == id } }
    }

    private val _intakes = MutableStateFlow<List<IntakeLog>>(
        dbHelper?.getAllIntakeLogs()?.ifEmpty { null } ?: listOf(
            IntakeLog(
                type = IntakeType.COFFEE,
                amount = 1.0f,
                unit = "cup",
                caffeineMg = 95,
                timeString = "8:30 AM",
                hourOfDay = 8,
                dateString = com.example.taskpulse.util.DateUtils.getDateWithOffset(-1),
                notes = "Morning pour-over coffee",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            IntakeLog(
                type = IntakeType.WATER,
                amount = 500f,
                unit = "ml",
                caffeineMg = 0,
                timeString = "10:15 AM",
                hourOfDay = 10,
                dateString = com.example.taskpulse.util.DateUtils.getDateWithOffset(-1),
                notes = "Chilled electrolyte water",
                timestamp = System.currentTimeMillis() - 80000000L
            ),
            IntakeLog(
                type = IntakeType.ENERGY_DRINK,
                amount = 1.0f,
                unit = "can",
                caffeineMg = 160,
                timeString = "1:15 PM",
                hourOfDay = 13,
                dateString = com.example.taskpulse.util.DateUtils.getDateWithOffset(-1),
                notes = "Afternoon focus booster",
                timestamp = System.currentTimeMillis() - 70000000L
            ),
            IntakeLog(
                type = IntakeType.WATER,
                amount = 500f,
                unit = "ml",
                caffeineMg = 0,
                timeString = "3:30 PM",
                hourOfDay = 15,
                dateString = com.example.taskpulse.util.DateUtils.getDateWithOffset(-1),
                notes = "Hydration check-in",
                timestamp = System.currentTimeMillis() - 60000000L
            )
        )
    )
    val intakes: StateFlow<List<IntakeLog>> = _intakes.asStateFlow()

    fun addIntake(intake: IntakeLog) {
        dbHelper?.insertIntakeLog(intake)
        _intakes.update { current -> listOf(intake) + current }
    }

    fun deleteIntake(id: String) {
        dbHelper?.deleteIntakeLog(id)
        _intakes.update { current -> current.filterNot { it.id == id } }
    }
}
