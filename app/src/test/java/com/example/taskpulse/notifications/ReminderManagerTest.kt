package com.example.taskpulse.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderManagerTest {

    @Test
    fun getSummaryText_whenDisabled_returnsRemindersOff() {
        val config = ReminderConfig(isEnabled = false, intervalHours = 1)
        assertEquals("Reminders Off", ReminderManager.getSummaryText(config))
    }

    @Test
    fun getSummaryText_withVariousIntervals_returnsCorrectLabels() {
        assertEquals("Every 1 hr", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 1)))
        assertEquals("Every 2 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 2)))
        assertEquals("Every 3 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 3)))
        assertEquals("Every 4 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 4)))
        assertEquals("Every 6 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 6)))
        assertEquals("Every 8 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 8)))
        assertEquals("Every 12 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 12)))
        assertEquals("Daily (24h)", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 24)))
        assertEquals("Every 5 hrs", ReminderManager.getSummaryText(ReminderConfig(isEnabled = true, intervalHours = 5)))
    }

    @Test
    fun reminderConfig_defaults_areSensible() {
        val config = ReminderConfig()
        assertTrue(config.isEnabled)
        assertEquals(2, config.intervalHours)
        assertEquals(8, config.startHour)
        assertEquals(22, config.endHour)
        assertTrue(config.quietHoursEnabled)
    }

    @Test
    fun getShortSummaryText_formatsConciseLabel() {
        val disabled = ReminderConfig(isEnabled = false, intervalHours = 2)
        assertEquals("Off", ReminderManager.getShortSummaryText(disabled))

        val oneHour = ReminderConfig(isEnabled = true, intervalHours = 1)
        assertEquals("1 hr", ReminderManager.getShortSummaryText(oneHour))

        val twoHours = ReminderConfig(isEnabled = true, intervalHours = 2)
        assertEquals("2 hr", ReminderManager.getShortSummaryText(twoHours))

        val fourHours = ReminderConfig(isEnabled = true, intervalHours = 4)
        assertEquals("4 hr", ReminderManager.getShortSummaryText(fourHours))
    }
}
