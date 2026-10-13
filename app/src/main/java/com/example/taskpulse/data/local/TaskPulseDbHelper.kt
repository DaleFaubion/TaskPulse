package com.example.taskpulse.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.taskpulse.data.model.Habit
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.data.model.Task

import com.example.taskpulse.data.model.HourlyCheckIn
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.data.model.SleepLog
import com.example.taskpulse.data.model.TimeClassification
import com.example.taskpulse.util.DateUtils

class TaskPulseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "taskpulse_local.db"
        const val DATABASE_VERSION = 6

        // Tasks Table
        const val TABLE_TASKS = "tasks"
        const val COLUMN_TASK_ID = "id"
        const val COLUMN_TASK_TITLE = "title"
        const val COLUMN_TASK_DESC = "description"
        const val COLUMN_TASK_PRIO = "priority"
        const val COLUMN_TASK_CAT = "category"
        const val COLUMN_TASK_DUE = "dueDate"
        const val COLUMN_TASK_COMPLETED = "isCompleted"

        // Habits Table
        const val TABLE_HABITS = "habits"
        const val COLUMN_HABIT_ID = "id"
        const val COLUMN_HABIT_TITLE = "title"
        const val COLUMN_HABIT_STREAK = "streakCount"
        const val COLUMN_HABIT_BEST = "bestStreak"
        const val COLUMN_HABIT_CAT = "category"
        const val COLUMN_HABIT_COMPLETED = "isCompletedToday"
        const val COLUMN_HABIT_DAYS = "completedDays"
        const val COLUMN_HABIT_TARGET = "targetDaysPerWeek"

        // Hourly Check-Ins Table
        const val TABLE_CHECKINS = "hourly_checkins"
        const val COLUMN_CHECKIN_ID = "id"
        const val COLUMN_CHECKIN_TIME = "timestamp"
        const val COLUMN_CHECKIN_DATE = "dateString"
        const val COLUMN_CHECKIN_SLOT = "timeSlot"
        const val COLUMN_CHECKIN_SUMMARY = "summary"
        const val COLUMN_CHECKIN_RATING = "productivityRating"
        const val COLUMN_CHECKIN_CLASS = "classification"
        const val COLUMN_CHECKIN_TAG = "categoryTag"

        // Sleep Logs Table
        const val TABLE_SLEEP = "sleep_logs"
        const val COLUMN_SLEEP_ID = "id"
        const val COLUMN_SLEEP_DATE = "dateString"
        const val COLUMN_SLEEP_HOURS = "hours"
        const val COLUMN_SLEEP_QUALITY = "qualityRating"
        const val COLUMN_SLEEP_NOTES = "notes"
        const val COLUMN_SLEEP_BEDTIME = "bedtime"
        const val COLUMN_SLEEP_WAKETIME = "wakeTime"
        const val COLUMN_SLEEP_TIME = "timestamp"

        // Intake Logs Table (Coffee, Water, Energy Drinks, etc.)
        const val TABLE_INTAKE = "intake_logs"
        const val COLUMN_INTAKE_ID = "id"
        const val COLUMN_INTAKE_TYPE = "type"
        const val COLUMN_INTAKE_AMOUNT = "amount"
        const val COLUMN_INTAKE_UNIT = "unit"
        const val COLUMN_INTAKE_CAFFEINE = "caffeineMg"
        const val COLUMN_INTAKE_TIME = "timeString"
        const val COLUMN_INTAKE_HOUR = "hourOfDay"
        const val COLUMN_INTAKE_DATE = "dateString"
        const val COLUMN_INTAKE_NOTES = "notes"
        const val COLUMN_INTAKE_TIMESTAMP = "timestamp"

        fun parseDates(str: String?): Set<String> {
            if (str.isNullOrBlank()) return emptySet()
            val todayIdx = DateUtils.currentDayOfWeekIndex
            return str.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { item ->
                    if (item.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                        item
                    } else {
                        val dayInt = item.toIntOrNull()
                        if (dayInt != null && dayInt in 0..6) {
                            val diff = dayInt - todayIdx
                            DateUtils.getDateWithOffset(diff)
                        } else null
                    }
                }
                .toSet()
        }

        fun formatDates(dates: Set<String>): String {
            return dates.sorted().joinToString(",")
        }

        fun parseDays(str: String?): Set<Int> {
            val dates = parseDates(str)
            return dates.mapNotNull { d ->
                try {
                    val cal = java.util.Calendar.getInstance()
                    val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(d)
                    if (parsed != null) {
                        cal.time = parsed
                        (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                    } else null
                } catch (_: Exception) { null }
            }.toSet()
        }

        fun formatDays(days: Set<Int>): String {
            return days.sorted().joinToString(",")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_TASK_ID TEXT PRIMARY KEY,
                $COLUMN_TASK_TITLE TEXT NOT NULL,
                $COLUMN_TASK_DESC TEXT,
                $COLUMN_TASK_PRIO TEXT NOT NULL,
                $COLUMN_TASK_CAT TEXT NOT NULL,
                $COLUMN_TASK_DUE TEXT NOT NULL,
                $COLUMN_TASK_COMPLETED INTEGER NOT NULL
            )
        """.trimIndent()

        val createHabitsTable = """
            CREATE TABLE $TABLE_HABITS (
                $COLUMN_HABIT_ID TEXT PRIMARY KEY,
                $COLUMN_HABIT_TITLE TEXT NOT NULL,
                $COLUMN_HABIT_STREAK INTEGER NOT NULL,
                $COLUMN_HABIT_BEST INTEGER NOT NULL,
                $COLUMN_HABIT_CAT TEXT NOT NULL,
                $COLUMN_HABIT_COMPLETED INTEGER NOT NULL,
                $COLUMN_HABIT_DAYS TEXT NOT NULL DEFAULT '',
                $COLUMN_HABIT_TARGET INTEGER NOT NULL DEFAULT 7
            )
        """.trimIndent()

        val createCheckInsTable = """
            CREATE TABLE $TABLE_CHECKINS (
                $COLUMN_CHECKIN_ID TEXT PRIMARY KEY,
                $COLUMN_CHECKIN_TIME INTEGER NOT NULL,
                $COLUMN_CHECKIN_DATE TEXT NOT NULL DEFAULT '',
                $COLUMN_CHECKIN_SLOT TEXT NOT NULL,
                $COLUMN_CHECKIN_SUMMARY TEXT NOT NULL,
                $COLUMN_CHECKIN_RATING INTEGER NOT NULL,
                $COLUMN_CHECKIN_CLASS TEXT NOT NULL,
                $COLUMN_CHECKIN_TAG TEXT NOT NULL
            )
        """.trimIndent()

        val createSleepTable = """
            CREATE TABLE $TABLE_SLEEP (
                $COLUMN_SLEEP_ID TEXT PRIMARY KEY,
                $COLUMN_SLEEP_DATE TEXT NOT NULL,
                $COLUMN_SLEEP_HOURS REAL NOT NULL,
                $COLUMN_SLEEP_QUALITY INTEGER NOT NULL,
                $COLUMN_SLEEP_NOTES TEXT NOT NULL,
                $COLUMN_SLEEP_BEDTIME TEXT NOT NULL,
                $COLUMN_SLEEP_WAKETIME TEXT NOT NULL,
                $COLUMN_SLEEP_TIME INTEGER NOT NULL
            )
        """.trimIndent()

        val createIntakeTable = """
            CREATE TABLE $TABLE_INTAKE (
                $COLUMN_INTAKE_ID TEXT PRIMARY KEY,
                $COLUMN_INTAKE_TYPE TEXT NOT NULL,
                $COLUMN_INTAKE_AMOUNT REAL NOT NULL,
                $COLUMN_INTAKE_UNIT TEXT NOT NULL,
                $COLUMN_INTAKE_CAFFEINE INTEGER NOT NULL,
                $COLUMN_INTAKE_TIME TEXT NOT NULL,
                $COLUMN_INTAKE_HOUR INTEGER NOT NULL,
                $COLUMN_INTAKE_DATE TEXT NOT NULL,
                $COLUMN_INTAKE_NOTES TEXT NOT NULL,
                $COLUMN_INTAKE_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTasksTable)
        db.execSQL(createHabitsTable)
        db.execSQL(createCheckInsTable)
        db.execSQL(createSleepTable)
        db.execSQL(createIntakeTable)

        // Seed initial items for first launch
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_HABITS ADD COLUMN $COLUMN_HABIT_DAYS TEXT NOT NULL DEFAULT ''")
            } catch (_: Exception) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_CHECKINS (
                        $COLUMN_CHECKIN_ID TEXT PRIMARY KEY,
                        $COLUMN_CHECKIN_TIME INTEGER NOT NULL,
                        $COLUMN_CHECKIN_DATE TEXT NOT NULL DEFAULT '',
                        $COLUMN_CHECKIN_SLOT TEXT NOT NULL,
                        $COLUMN_CHECKIN_SUMMARY TEXT NOT NULL,
                        $COLUMN_CHECKIN_RATING INTEGER NOT NULL,
                        $COLUMN_CHECKIN_CLASS TEXT NOT NULL,
                        $COLUMN_CHECKIN_TAG TEXT NOT NULL
                    )
                """.trimIndent())
            } catch (_: Exception) {}
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_SLEEP (
                        $COLUMN_SLEEP_ID TEXT PRIMARY KEY,
                        $COLUMN_SLEEP_DATE TEXT NOT NULL,
                        $COLUMN_SLEEP_HOURS REAL NOT NULL,
                        $COLUMN_SLEEP_QUALITY INTEGER NOT NULL,
                        $COLUMN_SLEEP_NOTES TEXT NOT NULL,
                        $COLUMN_SLEEP_BEDTIME TEXT NOT NULL,
                        $COLUMN_SLEEP_WAKETIME TEXT NOT NULL,
                        $COLUMN_SLEEP_TIME INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE $TABLE_HABITS ADD COLUMN $COLUMN_HABIT_TARGET INTEGER NOT NULL DEFAULT 7")
            } catch (_: Exception) {}
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_INTAKE (
                        $COLUMN_INTAKE_ID TEXT PRIMARY KEY,
                        $COLUMN_INTAKE_TYPE TEXT NOT NULL,
                        $COLUMN_INTAKE_AMOUNT REAL NOT NULL,
                        $COLUMN_INTAKE_UNIT TEXT NOT NULL,
                        $COLUMN_INTAKE_CAFFEINE INTEGER NOT NULL,
                        $COLUMN_INTAKE_TIME TEXT NOT NULL,
                        $COLUMN_INTAKE_HOUR INTEGER NOT NULL,
                        $COLUMN_INTAKE_DATE TEXT NOT NULL,
                        $COLUMN_INTAKE_NOTES TEXT NOT NULL,
                        $COLUMN_INTAKE_TIMESTAMP INTEGER NOT NULL
                    )
                """.trimIndent())
            } catch (_: Exception) {}
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE $TABLE_CHECKINS ADD COLUMN $COLUMN_CHECKIN_DATE TEXT NOT NULL DEFAULT ''")
            } catch (_: Exception) {}
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val initialTasks = listOf(
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

        for (task in initialTasks) {
            val values = ContentValues().apply {
                put(COLUMN_TASK_ID, task.id)
                put(COLUMN_TASK_TITLE, task.title)
                put(COLUMN_TASK_DESC, task.description)
                put(COLUMN_TASK_PRIO, task.priority.name)
                put(COLUMN_TASK_CAT, task.category)
                put(COLUMN_TASK_DUE, task.dueDate)
                put(COLUMN_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
            }
            db.insert(TABLE_TASKS, null, values)
        }

        val initialHabits = listOf(
            Habit(
                title = "Morning Meditation",
                streakCount = 2,
                bestStreak = 14,
                category = "Mindfulness",
                completedDates = setOf(DateUtils.getDateWithOffset(-1), DateUtils.getDateWithOffset(-2))
            ),
            Habit(
                title = "Drink 2.5L Water",
                streakCount = 3,
                bestStreak = 12,
                category = "Health",
                completedDates = setOf(DateUtils.getDateWithOffset(-1), DateUtils.getDateWithOffset(-2), DateUtils.getDateWithOffset(-3))
            ),
            Habit(
                title = "Code Android 45 Mins",
                streakCount = 1,
                bestStreak = 9,
                category = "Learning",
                completedDates = setOf(DateUtils.getDateWithOffset(-1))
            ),
            Habit(
                title = "Evening Reading",
                streakCount = 0,
                bestStreak = 10,
                category = "Growth",
                completedDates = setOf(DateUtils.getDateWithOffset(-2))
            )
        )

        for (habit in initialHabits) {
            val values = ContentValues().apply {
                put(COLUMN_HABIT_ID, habit.id)
                put(COLUMN_HABIT_TITLE, habit.title)
                put(COLUMN_HABIT_STREAK, habit.streakCount)
                put(COLUMN_HABIT_BEST, habit.bestStreak)
                put(COLUMN_HABIT_CAT, habit.category)
                put(COLUMN_HABIT_COMPLETED, if (habit.isCompletedToday) 1 else 0)
                put(COLUMN_HABIT_DAYS, formatDates(habit.completedDates))
                put(COLUMN_HABIT_TARGET, habit.targetDaysPerWeek)
            }
            db.insert(TABLE_HABITS, null, values)
        }

        val initialCheckIns = listOf(
            HourlyCheckIn(
                timeSlot = "9:00 AM - 10:00 AM",
                summary = "Team standup & reviewed backlog sprint goals",
                productivityRating = 4,
                classification = TimeClassification.PRODUCTIVE,
                categoryTag = "Planning",
                dateString = DateUtils.todayDateString
            ),
            HourlyCheckIn(
                timeSlot = "10:00 AM - 11:00 AM",
                summary = "Deep focus: Implemented core architecture & persistence logic",
                productivityRating = 5,
                classification = TimeClassification.PRODUCTIVE,
                categoryTag = "Coding",
                dateString = DateUtils.todayDateString
            ),
            HourlyCheckIn(
                timeSlot = "11:00 AM - 12:00 PM",
                summary = "Coffee break, walked outside & stretched",
                productivityRating = 4,
                classification = TimeClassification.RECHARGE,
                categoryTag = "Break",
                dateString = DateUtils.todayDateString
            )
        )

        for (checkIn in initialCheckIns) {
            val values = ContentValues().apply {
                put(COLUMN_CHECKIN_ID, checkIn.id)
                put(COLUMN_CHECKIN_TIME, checkIn.timestamp)
                put(COLUMN_CHECKIN_DATE, checkIn.dateString)
                put(COLUMN_CHECKIN_SLOT, checkIn.timeSlot)
                put(COLUMN_CHECKIN_SUMMARY, checkIn.summary)
                put(COLUMN_CHECKIN_RATING, checkIn.productivityRating)
                put(COLUMN_CHECKIN_CLASS, checkIn.classification.name)
                put(COLUMN_CHECKIN_TAG, checkIn.categoryTag)
            }
            db.insert(TABLE_CHECKINS, null, values)
        }

        val initialSleepLogs = listOf(
            SleepLog(
                dateString = "Today",
                hours = 7.5f,
                qualityRating = 4,
                notes = "Fell asleep smoothly, woke up energized",
                bedtime = "11:15 PM",
                wakeTime = "6:45 AM",
                timestamp = System.currentTimeMillis()
            ),
            SleepLog(
                dateString = "Yesterday",
                hours = 8.2f,
                qualityRating = 5,
                notes = "Deep, restorative REM cycles, no screens before bed",
                bedtime = "10:45 PM",
                wakeTime = "7:00 AM",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            SleepLog(
                dateString = "2 Days Ago",
                hours = 5.8f,
                qualityRating = 2,
                notes = "Late energy drink at 5:30 PM caused difficulty falling asleep",
                bedtime = "1:15 AM",
                wakeTime = "7:00 AM",
                timestamp = System.currentTimeMillis() - 172800000L
            ),
            SleepLog(
                dateString = "3 Days Ago",
                hours = 7.8f,
                qualityRating = 4,
                notes = "Hydrated well throughout day, productive focus",
                bedtime = "11:00 PM",
                wakeTime = "6:50 AM",
                timestamp = System.currentTimeMillis() - 259200000L
            )
        )
        for (sleep in initialSleepLogs) {
            val sleepValues = ContentValues().apply {
                put(COLUMN_SLEEP_ID, sleep.id)
                put(COLUMN_SLEEP_DATE, sleep.dateString)
                put(COLUMN_SLEEP_HOURS, sleep.hours)
                put(COLUMN_SLEEP_QUALITY, sleep.qualityRating)
                put(COLUMN_SLEEP_NOTES, sleep.notes)
                put(COLUMN_SLEEP_BEDTIME, sleep.bedtime)
                put(COLUMN_SLEEP_WAKETIME, sleep.wakeTime)
                put(COLUMN_SLEEP_TIME, sleep.timestamp)
            }
            db.insert(TABLE_SLEEP, null, sleepValues)
        }

        val initialIntakes = listOf(
            IntakeLog(
                type = IntakeType.COFFEE,
                amount = 1.0f,
                unit = "cup",
                caffeineMg = 95,
                timeString = "8:30 AM",
                hourOfDay = 8,
                dateString = DateUtils.getDateWithOffset(-1),
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
                dateString = DateUtils.getDateWithOffset(-1),
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
                dateString = DateUtils.getDateWithOffset(-1),
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
                dateString = DateUtils.getDateWithOffset(-1),
                notes = "Hydration check-in",
                timestamp = System.currentTimeMillis() - 60000000L
            )
        )
        for (intake in initialIntakes) {
            val intakeValues = ContentValues().apply {
                put(COLUMN_INTAKE_ID, intake.id)
                put(COLUMN_INTAKE_TYPE, intake.type.name)
                put(COLUMN_INTAKE_AMOUNT, intake.amount)
                put(COLUMN_INTAKE_UNIT, intake.unit)
                put(COLUMN_INTAKE_CAFFEINE, intake.caffeineMg)
                put(COLUMN_INTAKE_TIME, intake.timeString)
                put(COLUMN_INTAKE_HOUR, intake.hourOfDay)
                put(COLUMN_INTAKE_DATE, intake.dateString)
                put(COLUMN_INTAKE_NOTES, intake.notes)
                put(COLUMN_INTAKE_TIMESTAMP, intake.timestamp)
            }
            db.insert(TABLE_INTAKE, null, intakeValues)
        }
    }

    // Task Operations
    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TASKS, null, null, null, null, null, null)
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COLUMN_TASK_ID)
            val titleIdx = it.getColumnIndexOrThrow(COLUMN_TASK_TITLE)
            val descIdx = it.getColumnIndexOrThrow(COLUMN_TASK_DESC)
            val prioIdx = it.getColumnIndexOrThrow(COLUMN_TASK_PRIO)
            val catIdx = it.getColumnIndexOrThrow(COLUMN_TASK_CAT)
            val dueIdx = it.getColumnIndexOrThrow(COLUMN_TASK_DUE)
            val compIdx = it.getColumnIndexOrThrow(COLUMN_TASK_COMPLETED)

            while (it.moveToNext()) {
                val priority = try {
                    Priority.valueOf(it.getString(prioIdx))
                } catch (_: Exception) {
                    Priority.MEDIUM
                }
                tasks.add(
                    Task(
                        id = it.getString(idIdx),
                        title = it.getString(titleIdx),
                        description = it.getString(descIdx) ?: "",
                        priority = priority,
                        category = it.getString(catIdx) ?: "General",
                        dueDate = it.getString(dueIdx) ?: "Today",
                        isCompleted = it.getInt(compIdx) == 1
                    )
                )
            }
        }
        return tasks
    }

    fun insertTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_ID, task.id)
            put(COLUMN_TASK_TITLE, task.title)
            put(COLUMN_TASK_DESC, task.description)
            put(COLUMN_TASK_PRIO, task.priority.name)
            put(COLUMN_TASK_CAT, task.category)
            put(COLUMN_TASK_DUE, task.dueDate)
            put(COLUMN_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_TASKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_TITLE, task.title)
            put(COLUMN_TASK_DESC, task.description)
            put(COLUMN_TASK_PRIO, task.priority.name)
            put(COLUMN_TASK_CAT, task.category)
            put(COLUMN_TASK_DUE, task.dueDate)
            put(COLUMN_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.update(TABLE_TASKS, values, "$COLUMN_TASK_ID = ?", arrayOf(task.id))
    }

    fun deleteTask(taskId: String) {
        val db = writableDatabase
        db.delete(TABLE_TASKS, "$COLUMN_TASK_ID = ?", arrayOf(taskId))
    }

    // Habit Operations
    fun getAllHabits(): List<Habit> {
        val habits = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.query(TABLE_HABITS, null, null, null, null, null, null)
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COLUMN_HABIT_ID)
            val titleIdx = it.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)
            val bestIdx = it.getColumnIndexOrThrow(COLUMN_HABIT_BEST)
            val catIdx = it.getColumnIndexOrThrow(COLUMN_HABIT_CAT)
            val daysIdx = it.getColumnIndex(COLUMN_HABIT_DAYS)
            val targetIdx = it.getColumnIndex(COLUMN_HABIT_TARGET)

            while (it.moveToNext()) {
                val rawDays = if (daysIdx >= 0 && !it.isNull(daysIdx)) it.getString(daysIdx) else ""
                val completedDates = parseDates(rawDays)
                val streak = DateUtils.calculateStreak(completedDates)
                val storedBest = it.getInt(bestIdx)
                val bestStreak = maxOf(storedBest, streak)
                val targetDays = if (targetIdx >= 0 && !it.isNull(targetIdx)) it.getInt(targetIdx) else 7

                habits.add(
                    Habit(
                        id = it.getString(idIdx),
                        title = it.getString(titleIdx),
                        streakCount = streak,
                        bestStreak = bestStreak,
                        category = it.getString(catIdx) ?: "Health",
                        completedDates = completedDates,
                        targetDaysPerWeek = targetDays
                    )
                )
            }
        }
        return habits
    }

    fun insertHabit(habit: Habit) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HABIT_ID, habit.id)
            put(COLUMN_HABIT_TITLE, habit.title)
            put(COLUMN_HABIT_STREAK, habit.streakCount)
            put(COLUMN_HABIT_BEST, habit.bestStreak)
            put(COLUMN_HABIT_CAT, habit.category)
            put(COLUMN_HABIT_COMPLETED, if (habit.isCompletedToday) 1 else 0)
            put(COLUMN_HABIT_DAYS, formatDates(habit.completedDates))
            put(COLUMN_HABIT_TARGET, habit.targetDaysPerWeek)
        }
        db.insertWithOnConflict(TABLE_HABITS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateHabit(habit: Habit) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HABIT_TITLE, habit.title)
            put(COLUMN_HABIT_STREAK, habit.streakCount)
            put(COLUMN_HABIT_BEST, habit.bestStreak)
            put(COLUMN_HABIT_CAT, habit.category)
            put(COLUMN_HABIT_COMPLETED, if (habit.isCompletedToday) 1 else 0)
            put(COLUMN_HABIT_DAYS, formatDates(habit.completedDates))
            put(COLUMN_HABIT_TARGET, habit.targetDaysPerWeek)
        }
        db.update(TABLE_HABITS, values, "$COLUMN_HABIT_ID = ?", arrayOf(habit.id))
    }

    fun deleteHabit(habitId: String) {
        val db = writableDatabase
        db.delete(TABLE_HABITS, "$COLUMN_HABIT_ID = ?", arrayOf(habitId))
    }

    // Hourly Check-In Operations
    fun getAllCheckIns(): List<HourlyCheckIn> {
        val checkIns = mutableListOf<HourlyCheckIn>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CHECKINS, null, null, null, null, null, "$COLUMN_CHECKIN_TIME DESC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_ID)
            val timeIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_TIME)
            val dateIdx = it.getColumnIndex(COLUMN_CHECKIN_DATE)
            val slotIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_SLOT)
            val sumIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_SUMMARY)
            val rateIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_RATING)
            val classIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_CLASS)
            val tagIdx = it.getColumnIndexOrThrow(COLUMN_CHECKIN_TAG)

            while (it.moveToNext()) {
                val classification = try {
                    TimeClassification.valueOf(it.getString(classIdx))
                } catch (_: Exception) {
                    TimeClassification.PRODUCTIVE
                }
                val rawDate = if (dateIdx >= 0 && !it.isNull(dateIdx)) it.getString(dateIdx) else ""
                val finalDate = if (rawDate.isNotBlank()) rawDate else DateUtils.formatTimestampToDate(it.getLong(timeIdx))
                checkIns.add(
                    HourlyCheckIn(
                        id = it.getString(idIdx),
                        timestamp = it.getLong(timeIdx),
                        dateString = finalDate,
                        timeSlot = it.getString(slotIdx),
                        summary = it.getString(sumIdx),
                        productivityRating = it.getInt(rateIdx),
                        classification = classification,
                        categoryTag = it.getString(tagIdx) ?: "Work"
                    )
                )
            }
        }
        return checkIns
    }

    fun insertCheckIn(checkIn: HourlyCheckIn) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHECKIN_ID, checkIn.id)
            put(COLUMN_CHECKIN_TIME, checkIn.timestamp)
            put(COLUMN_CHECKIN_DATE, checkIn.dateString)
            put(COLUMN_CHECKIN_SLOT, checkIn.timeSlot)
            put(COLUMN_CHECKIN_SUMMARY, checkIn.summary)
            put(COLUMN_CHECKIN_RATING, checkIn.productivityRating)
            put(COLUMN_CHECKIN_CLASS, checkIn.classification.name)
            put(COLUMN_CHECKIN_TAG, checkIn.categoryTag)
        }
        db.insertWithOnConflict(TABLE_CHECKINS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteCheckIn(checkInId: String) {
        val db = writableDatabase
        db.delete(TABLE_CHECKINS, "$COLUMN_CHECKIN_ID = ?", arrayOf(checkInId))
    }

    // Sleep Log Operations
    fun getAllSleepLogs(): List<SleepLog> {
        val list = mutableListOf<SleepLog>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SLEEP, null, null, null, null, null, "$COLUMN_SLEEP_TIME DESC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_ID)
            val dateIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_DATE)
            val hoursIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_HOURS)
            val qualityIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_QUALITY)
            val notesIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_NOTES)
            val bedIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_BEDTIME)
            val wakeIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_WAKETIME)
            val timeIdx = it.getColumnIndexOrThrow(COLUMN_SLEEP_TIME)

            while (it.moveToNext()) {
                list.add(
                    SleepLog(
                        id = it.getString(idIdx),
                        dateString = it.getString(dateIdx),
                        hours = it.getFloat(hoursIdx),
                        qualityRating = it.getInt(qualityIdx),
                        notes = it.getString(notesIdx),
                        bedtime = it.getString(bedIdx),
                        wakeTime = it.getString(wakeIdx),
                        timestamp = it.getLong(timeIdx)
                    )
                )
            }
        }
        return list
    }

    fun insertSleepLog(sleepLog: SleepLog) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SLEEP_ID, sleepLog.id)
            put(COLUMN_SLEEP_DATE, sleepLog.dateString)
            put(COLUMN_SLEEP_HOURS, sleepLog.hours)
            put(COLUMN_SLEEP_QUALITY, sleepLog.qualityRating)
            put(COLUMN_SLEEP_NOTES, sleepLog.notes)
            put(COLUMN_SLEEP_BEDTIME, sleepLog.bedtime)
            put(COLUMN_SLEEP_WAKETIME, sleepLog.wakeTime)
            put(COLUMN_SLEEP_TIME, sleepLog.timestamp)
        }
        db.insertWithOnConflict(TABLE_SLEEP, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteSleepLog(id: String) {
        val db = writableDatabase
        db.delete(TABLE_SLEEP, "$COLUMN_SLEEP_ID = ?", arrayOf(id))
    }

    // Intake Operations (Water, Coffee, Energy Drinks, Tea, etc.)
    fun getAllIntakeLogs(): List<IntakeLog> {
        val list = mutableListOf<IntakeLog>()
        val db = readableDatabase
        val cursor = db.query(TABLE_INTAKE, null, null, null, null, null, "$COLUMN_INTAKE_TIMESTAMP DESC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_ID)
            val typeIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_TYPE)
            val amountIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_AMOUNT)
            val unitIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_UNIT)
            val caffIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_CAFFEINE)
            val timeIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_TIME)
            val hourIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_HOUR)
            val dateIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_DATE)
            val notesIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_NOTES)
            val tsIdx = it.getColumnIndexOrThrow(COLUMN_INTAKE_TIMESTAMP)

            while (it.moveToNext()) {
                val typeName = it.getString(typeIdx)
                val type = IntakeType.fromString(typeName)
                val rawDate = it.getString(dateIdx) ?: ""
                val ts = it.getLong(tsIdx)
                val resolvedDate = if (rawDate.equals("Today", ignoreCase = true) && ts > 0L) {
                    DateUtils.formatTimestampToDate(ts)
                } else if (rawDate.isNotBlank() && !rawDate.equals("Today", ignoreCase = true)) {
                    rawDate
                } else if (ts > 0L) {
                    DateUtils.formatTimestampToDate(ts)
                } else {
                    DateUtils.todayDateString
                }

                list.add(
                    IntakeLog(
                        id = it.getString(idIdx),
                        type = type,
                        amount = it.getFloat(amountIdx),
                        unit = it.getString(unitIdx) ?: "cup",
                        caffeineMg = it.getInt(caffIdx),
                        timeString = it.getString(timeIdx) ?: "12:00 PM",
                        hourOfDay = it.getInt(hourIdx),
                        dateString = resolvedDate,
                        notes = it.getString(notesIdx) ?: "",
                        timestamp = ts
                    )
                )
            }
        }
        return list
    }

    fun insertIntakeLog(intake: IntakeLog) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INTAKE_ID, intake.id)
            put(COLUMN_INTAKE_TYPE, intake.type.name)
            put(COLUMN_INTAKE_AMOUNT, intake.amount)
            put(COLUMN_INTAKE_UNIT, intake.unit)
            put(COLUMN_INTAKE_CAFFEINE, intake.caffeineMg)
            put(COLUMN_INTAKE_TIME, intake.timeString)
            put(COLUMN_INTAKE_HOUR, intake.hourOfDay)
            put(COLUMN_INTAKE_DATE, intake.dateString)
            put(COLUMN_INTAKE_NOTES, intake.notes)
            put(COLUMN_INTAKE_TIMESTAMP, intake.timestamp)
        }
        db.insertWithOnConflict(TABLE_INTAKE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteIntakeLog(id: String) {
        val db = writableDatabase
        db.delete(TABLE_INTAKE, "$COLUMN_INTAKE_ID = ?", arrayOf(id))
    }
}
