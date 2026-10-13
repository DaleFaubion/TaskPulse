package com.example.taskpulse.ui

import com.example.taskpulse.data.TaskPulseRepository
import com.example.taskpulse.data.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskPulseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskPulseRepository
    private lateinit var viewModel: TaskPulseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = TaskPulseRepository()
        viewModel = TaskPulseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tasksAndHabits_reflectRepositoryState() = runTest {
        advanceUntilIdle()
        assertEquals(repository.tasks.value.size, viewModel.tasks.value.size)
        assertEquals(repository.habits.value.size, viewModel.habits.value.size)
    }

    @Test
    fun addTask_withValidTitle_addsTask() = runTest {
        advanceUntilIdle()
        val initialCount = viewModel.tasks.value.size

        viewModel.addTask(
            title = "  Write Unit Tests  ",
            description = "Ensure reliability",
            priority = Priority.HIGH,
            category = "QA"
        )
        advanceUntilIdle()

        assertEquals(initialCount + 1, viewModel.tasks.value.size)
        val addedTask = viewModel.tasks.value.first()
        assertEquals("Write Unit Tests", addedTask.title)
        assertEquals("Ensure reliability", addedTask.description)
        assertEquals(Priority.HIGH, addedTask.priority)
        assertEquals("QA", addedTask.category)
    }

    @Test
    fun addTask_withBlankTitle_doesNotAddTask() = runTest {
        advanceUntilIdle()
        val initialCount = viewModel.tasks.value.size

        viewModel.addTask(
            title = "   ",
            description = "Blank title",
            priority = Priority.LOW,
            category = "General"
        )
        advanceUntilIdle()

        assertEquals(initialCount, viewModel.tasks.value.size)
    }

    @Test
    fun addHabit_withValidTitle_addsHabit() = runTest {
        advanceUntilIdle()
        val initialCount = viewModel.habits.value.size

        viewModel.addHabit(title = " Daily Walk ", category = "")
        advanceUntilIdle()

        assertEquals(initialCount + 1, viewModel.habits.value.size)
        val added = viewModel.habits.value.first()
        assertEquals("Daily Walk", added.title)
        assertEquals("Health", added.category) // Default fallback category
    }

    @Test
    fun toggleTask_updatesStatus() = runTest {
        advanceUntilIdle()
        val task = viewModel.tasks.value.first()
        val previousState = task.isCompleted

        viewModel.toggleTask(task.id)
        advanceUntilIdle()

        val updated = viewModel.tasks.value.first { it.id == task.id }
        assertEquals(!previousState, updated.isCompleted)
    }

    @Test
    fun deleteTask_removesItem() = runTest {
        advanceUntilIdle()
        val task = viewModel.tasks.value.first()

        viewModel.deleteTask(task.id)
        advanceUntilIdle()

        assertFalse(viewModel.tasks.value.any { it.id == task.id })
    }

    @Test
    fun dashboardState_calculatesMetricsCorrectly() = runTest {
        // Collect in backgroundScope to activate WhileSubscribed StateFlow
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dashboardState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.dashboardState.value

        val tasks = viewModel.tasks.value
        val habits = viewModel.habits.value

        val expectedCompletedTasks = tasks.count { it.isCompleted }
        val expectedActiveStreaks = habits.sumOf { it.streakCount }

        assertEquals(expectedCompletedTasks, state.completedTasks)
        assertEquals(tasks.size, state.totalTasks)
        assertEquals(expectedActiveStreaks, state.activeHabitStreaks)
        assertEquals(habits.size, state.totalHabits)

        val totalItems = tasks.size + habits.size
        val completedItems = expectedCompletedTasks + habits.count { it.isCompletedToday }
        val expectedProgress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
        assertEquals(expectedProgress, state.progressFraction, 0.001f)
    }

    @Test
    fun toggleHabitDay_updatesHabitInState() = runTest {
        advanceUntilIdle()
        val habit = viewModel.habits.value.first()
        val testDay = 4 // Friday
        val initialStatus = testDay in habit.completedDays

        viewModel.toggleHabitDay(habit.id, testDay)
        advanceUntilIdle()

        val updated = viewModel.habits.value.first { it.id == habit.id }
        assertEquals(!initialStatus, testDay in updated.completedDays)
    }

    @Test
    fun addCheckIn_withValidData_addsCheckIn() = runTest {
        advanceUntilIdle()
        val initialCount = viewModel.checkIns.value.size

        viewModel.addCheckIn(
            timeSlot = "10:00 AM - 11:00 AM",
            summary = "Designed dashboard components",
            productivityRating = 4,
            classification = com.example.taskpulse.data.model.TimeClassification.PRODUCTIVE,
            categoryTag = "Work"
        )
        advanceUntilIdle()

        assertEquals(initialCount + 1, viewModel.checkIns.value.size)
        val added = viewModel.checkIns.value.first()
        assertEquals("Designed dashboard components", added.summary)
        assertEquals(4, added.productivityRating)
        assertEquals(com.example.taskpulse.data.model.TimeClassification.PRODUCTIVE, added.classification)
    }

    @Test
    fun deleteCheckIn_removesCheckIn() = runTest {
        advanceUntilIdle()
        val checkIn = viewModel.checkIns.value.first()

        viewModel.deleteCheckIn(checkIn.id)
        advanceUntilIdle()

        assertFalse(viewModel.checkIns.value.any { it.id == checkIn.id })
    }

    @Test
    fun logSleep_updatesSleepLogsAndDashboard() = runTest {
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dashboardState.collect {}
        }
        advanceUntilIdle()

        viewModel.logSleep(
            hours = 8.0f,
            qualityRating = 5,
            notes = "Great rest",
            bedtime = "11:00 PM",
            wakeTime = "7:00 AM"
        )
        advanceUntilIdle()

        val logs = viewModel.sleepLogs.value
        assertEquals(8.0f, logs.first().hours)
        assertEquals(5, logs.first().qualityRating)
        assertEquals("Great rest", logs.first().notes)
        assertEquals(8.0f, viewModel.dashboardState.value.todaySleepHours)
    }

    @Test
    fun generateExportCsv_returnsValidCsv() = runTest {
        advanceUntilIdle()
        val csv = viewModel.generateExportCsv()
        assertTrue(csv.contains("=== TASKPULSE DATA EXPORT ==="))
        assertTrue(csv.contains("--- TASKS ---"))
        assertTrue(csv.contains("--- HABITS ---"))
        assertTrue(csv.contains("--- HOURLY CHECK-INS ---"))
        assertTrue(csv.contains("--- SLEEP LOGS ---"))
    }

    @Test
    fun caffeineAndWaterResetEachNight_onlyCalculatesTodaysIntakes() = runTest {
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dashboardState.collect {}
        }
        advanceUntilIdle()

        val initialWater = viewModel.dashboardState.value.totalWaterMl
        val initialCaffeine = viewModel.dashboardState.value.totalCaffeineMg
        val initialCoffee = viewModel.dashboardState.value.coffeeCount
        val initialEnergy = viewModel.dashboardState.value.energyDrinkCount

        val yesterday = com.example.taskpulse.util.DateUtils.yesterdayDateString
        val today = com.example.taskpulse.util.DateUtils.todayDateString

        // Log drink for yesterday (e.g. 500ml water + 200mg caffeine)
        viewModel.addIntake(
            type = com.example.taskpulse.data.model.IntakeType.WATER,
            amount = 500f,
            unit = "ml",
            caffeineMg = 0,
            timeString = "3:00 PM",
            hourOfDay = 15,
            notes = "Yesterday water",
            dateString = yesterday
        )
        viewModel.addIntake(
            type = com.example.taskpulse.data.model.IntakeType.ENERGY_DRINK,
            amount = 1f,
            unit = "can",
            caffeineMg = 200,
            timeString = "2:00 PM",
            hourOfDay = 14,
            notes = "Yesterday energy drink",
            dateString = yesterday
        )
        advanceUntilIdle()

        // Yesterday's intake must NOT increase today's dashboard counters
        val stateAfterYesterday = viewModel.dashboardState.value
        assertEquals(initialWater, stateAfterYesterday.totalWaterMl, 0.01f)
        assertEquals(initialCaffeine, stateAfterYesterday.totalCaffeineMg)
        assertEquals(initialEnergy, stateAfterYesterday.energyDrinkCount)

        // Now log a drink for today
        viewModel.addIntake(
            type = com.example.taskpulse.data.model.IntakeType.WATER,
            amount = 250f,
            unit = "ml",
            caffeineMg = 0,
            timeString = "10:00 AM",
            hourOfDay = 10,
            notes = "Today water",
            dateString = today
        )
        viewModel.quickLogDrink(com.example.taskpulse.data.model.IntakeType.COFFEE) // Coffee adds 95mg caffeine
        advanceUntilIdle()

        val stateAfterToday = viewModel.dashboardState.value
        assertEquals(initialWater + 250f, stateAfterToday.totalWaterMl, 0.01f)
        assertEquals(initialCaffeine + 95, stateAfterToday.totalCaffeineMg)
        assertEquals(initialCoffee + 1, stateAfterToday.coffeeCount)
    }

    @Test
    fun toggleHabitDate_togglesCalendarDateInViewModel() = runTest {
        advanceUntilIdle()
        val habit = viewModel.habits.value.first()
        val yesterday = com.example.taskpulse.util.DateUtils.yesterdayDateString
        val initialContains = yesterday in habit.completedDates

        viewModel.toggleHabitDate(habit.id, yesterday)
        advanceUntilIdle()

        val updated = viewModel.habits.value.first { it.id == habit.id }
        assertEquals(!initialContains, yesterday in updated.completedDates)
    }
}
