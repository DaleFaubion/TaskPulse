package com.example.taskpulse.data.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val dueDate: String = "Today",
    val isCompleted: Boolean = false
)
