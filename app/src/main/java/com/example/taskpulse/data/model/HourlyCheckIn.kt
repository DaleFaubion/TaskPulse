package com.example.taskpulse.data.model

import com.example.taskpulse.util.DateUtils
import java.util.UUID

data class HourlyCheckIn(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = DateUtils.todayDateString,
    val timeSlot: String,
    val summary: String,
    val productivityRating: Int = 4, // 1 to 5 stars
    val classification: TimeClassification = TimeClassification.PRODUCTIVE,
    val categoryTag: String = "Work"
)

