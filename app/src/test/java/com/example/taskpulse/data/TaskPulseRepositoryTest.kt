package com.example.taskpulse.data

import com.example.taskpulse.data.model.Habit
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.data.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskPulseRepositoryTest {

    private lateinit var repository: TaskPulseRepository

    @Before
    fun setUp() {
        repository = TaskPulseRepository()
    }

    @Test
    fun repository_initialState_hasPreloadedTasksAndHabits() {
        val initialTasks = repository.tasks.value
        val initialHabits = repository.habits.value

        assertTrue(initialTasks.isNotEmpty())
        assertTrue(initialHabits.isNotEmpty())
    }

    @Test
    fun addTask_insertsAtBeginning() {
        val newTask = Task(
            title = "New Integration Test Task",
            description = "Testing repository insertion",
            priority = Priority.HIGH,
            category = "Testing"
        )

        repository.addTask(newTask)

        val tasks = repository.tasks.value
        assertEquals(newTask.id, tasks.first().id)
        assertEquals("New Integration Test Task", tasks.first().title)
    }

    @Test
    fun toggleTaskCompletion_togglesStatus() {
        val targetTask = repository.tasks.value.first()
        val initialStatus = targetTask.isCompleted

        repository.toggleTaskCompletion(targetTask.id)

        val updatedTask = repository.tasks.value.first { it.id == targetTask.id }
        assertEquals(!initialStatus, updatedTask.isCompleted)

        repository.toggleTaskCompletion(targetTask.id)
        val revertedTask = repository.tasks.value.first { it.id == targetTask.id }
        assertEquals(initialStatus, revertedTask.isCompleted)
    }

    @Test
    fun deleteTask_removesTaskFromList() {
        val targetTask = repository.tasks.value.first()
        repository.deleteTask(targetTask.id)

        val remainingTasks = repository.tasks.value
        assertNull(remainingTasks.find { it.id == targetTask.id })
    }

    @Test
    fun addHabit_insertsAtBeginning() {
        val newHabit = Habit(
            title = "10k Daily Steps",
            category = "Fitness"
        )

        repository.addHabit(newHabit)

        val habits = repository.habits.value
        assertEquals(newHabit.id, habits.first().id)
        assertEquals("10k Daily Steps", habits.first().title)
    }

    @Test
    fun toggleHabitCompletion_updatesStreakAndBest() {
        val uncompletedHabit = repository.habits.value.first { !it.isCompletedToday }
        val initialStreak = uncompletedHabit.streakCount
        val initialBest = uncompletedHabit.bestStreak

        repository.toggleHabitCompletion(uncompletedHabit.id)

        val updatedHabit = repository.habits.value.first { it.id == uncompletedHabit.id }
        assertTrue(updatedHabit.isCompletedToday)
        assertEquals(initialStreak + 1, updatedHabit.streakCount)
        assertTrue(updatedHabit.bestStreak >= initialBest)

        repository.toggleHabitCompletion(uncompletedHabit.id)
        val uncheckedHabit = repository.habits.value.first { it.id == uncompletedHabit.id }
        assertFalse(uncheckedHabit.isCompletedToday)
        assertEquals(initialStreak, uncheckedHabit.streakCount)
    }

    @Test
    fun deleteHabit_removesHabitFromList() {
        val targetHabit = repository.habits.value.first()
        repository.deleteHabit(targetHabit.id)

        val remainingHabits = repository.habits.value
        assertNull(remainingHabits.find { it.id == targetHabit.id })
    }

    @Test
    fun toggleHabitDay_togglesDayInCompletedDays() {
        val targetHabit = repository.habits.value.first()
        val testDay = 2 // Wednesday

        val initialContains = testDay in targetHabit.completedDays
        repository.toggleHabitDay(targetHabit.id, testDay)

        val updated = repository.habits.value.first { it.id == targetHabit.id }
        assertEquals(!initialContains, testDay in updated.completedDays)

        repository.toggleHabitDay(targetHabit.id, testDay)
        val reverted = repository.habits.value.first { it.id == targetHabit.id }
        assertEquals(initialContains, testDay in reverted.completedDays)
    }

    @Test
    fun toggleHabitCompletion_updatesTodayInCompletedDays() {
        val today = com.example.taskpulse.util.DateUtils.currentDayOfWeekIndex
        val uncompleted = repository.habits.value.first { !it.isCompletedToday }

        repository.toggleHabitCompletion(uncompleted.id)
        val done = repository.habits.value.first { it.id == uncompleted.id }
        assertTrue(done.isCompletedToday)
        assertTrue(today in done.completedDays)

        repository.toggleHabitCompletion(uncompleted.id)
        val undone = repository.habits.value.first { it.id == uncompleted.id }
        assertFalse(undone.isCompletedToday)
        assertFalse(today in undone.completedDays)
    }

    @Test
    fun addCheckIn_insertsAtBeginning() {
        val checkIn = com.example.taskpulse.data.model.HourlyCheckIn(
            timeSlot = "02:00 PM - 03:00 PM",
            summary = "Refactored UI components",
            productivityRating = 5,
            classification = com.example.taskpulse.data.model.TimeClassification.PRODUCTIVE,
            categoryTag = "Work"
        )
        val initialCount = repository.checkIns.value.size
        repository.addCheckIn(checkIn)

        val updated = repository.checkIns.value
        assertEquals(initialCount + 1, updated.size)
        assertEquals(checkIn.id, updated.first().id)
        assertEquals("Refactored UI components", updated.first().summary)
        assertEquals(5, updated.first().productivityRating)
        assertEquals(com.example.taskpulse.data.model.TimeClassification.PRODUCTIVE, updated.first().classification)
    }

    @Test
    fun deleteCheckIn_removesCheckInFromList() {
        val checkIn = repository.checkIns.value.first()
        repository.deleteCheckIn(checkIn.id)

        val remaining = repository.checkIns.value
        assertNull(remaining.find { it.id == checkIn.id })
    }

    @Test
    fun toggleHabitForDate_togglesSpecificCalendarDate() {
        val habit = repository.habits.value.first()
        val yesterday = com.example.taskpulse.util.DateUtils.yesterdayDateString

        val initialContains = yesterday in habit.completedDates
        repository.toggleHabitForDate(habit.id, yesterday)

        val updated = repository.habits.value.first { it.id == habit.id }
        assertEquals(!initialContains, yesterday in updated.completedDates)

        repository.toggleHabitForDate(habit.id, yesterday)
        val reverted = repository.habits.value.first { it.id == habit.id }
        assertEquals(initialContains, yesterday in reverted.completedDates)
    }

    @Test
    fun addCheckIn_withCustomDate_storesExactDateString() {
        val yesterday = com.example.taskpulse.util.DateUtils.yesterdayDateString
        val checkIn = com.example.taskpulse.data.model.HourlyCheckIn(
            dateString = yesterday,
            timeSlot = "04:00 PM - 05:00 PM",
            summary = "Past day sprint planning",
            productivityRating = 4,
            classification = com.example.taskpulse.data.model.TimeClassification.PRODUCTIVE,
            categoryTag = "Work"
        )
        repository.addCheckIn(checkIn)

        val found = repository.checkIns.value.first { it.id == checkIn.id }
        assertEquals(yesterday, found.dateString)
    }
}
