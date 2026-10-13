package com.example.taskpulse.data.model

enum class TimeClassification(val displayName: String, val emoji: String) {
    PRODUCTIVE("Productive", "⚡"),
    RECHARGE("Recharge", "☕"),
    NEUTRAL("Neutral", "⚖️"),
    WASTED("Wasted Time", "🛑")
}
