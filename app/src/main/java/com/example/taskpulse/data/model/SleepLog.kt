package com.example.taskpulse.data.model

import com.example.taskpulse.util.DateUtils
import java.util.UUID

data class SleepLog(
    val id: String = UUID.randomUUID().toString(),
    val dateString: String = DateUtils.todayDateString, // e.g. "2026-10-12"
    val hours: Float,       // e.g. 7.5f
    val qualityRating: Int = 4, // 1 to 5
    val notes: String = "",
    val bedtime: String = "11:30 PM",
    val wakeTime: String = "7:00 AM",
    val timestamp: Long = System.currentTimeMillis()
)
