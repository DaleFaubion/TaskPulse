package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassBorder
import com.example.taskpulse.theme.GlassSurface
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary
import com.example.taskpulse.ui.DashboardUiState

@Composable
fun DailyDebriefDialog(
    dashboardState: DashboardUiState,
    initialReflection: String = "",
    onDismiss: () -> Unit,
    onSaveReflection: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var reflectionNote by remember { mutableStateOf(initialReflection) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GlassSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌙", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Debrief & Reflection",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Review your accomplishments today",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Scorecard Grid (2x2)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DebriefCard(
                        title = "Tasks Done",
                        value = "${dashboardState.completedTasks} / ${dashboardState.totalTasks}",
                        color = EmeraldGreen,
                        emoji = "✅",
                        modifier = Modifier.weight(1f)
                    )
                    DebriefCard(
                        title = "Habits Active",
                        value = "🔥 ${dashboardState.activeHabitStreaks}d",
                        color = ElectricCyan,
                        emoji = "⚡",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DebriefCard(
                        title = "Focus Time",
                        value = "${dashboardState.productiveHours}h Productive",
                        color = NeonViolet,
                        emoji = "⏱️",
                        modifier = Modifier.weight(1f)
                    )
                    DebriefCard(
                        title = "Sleep Rested",
                        value = if (dashboardState.todaySleepHours > 0) "${"%.1f".format(dashboardState.todaySleepHours)} hrs" else "Not Logged",
                        color = AmberGlow,
                        emoji = "🌙",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Productivity Score Headline
                val ratingScore = dashboardState.avgProductivityRating
                val headline = when {
                    ratingScore >= 4.5f -> "🔥 Exceptional focus day! You crushed your objectives."
                    ratingScore >= 3.5f -> "⚡ Solid, consistent momentum today. Keep building!"
                    ratingScore >= 2.0f -> "⚖️ Balanced effort today. Tomorrow is a fresh start."
                    else -> "🌱 Log your hourly check-ins to track your focus score."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurfaceVariant)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = headline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reflection Input
                OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = { reflectionNote = it },
                    label = { Text("Daily Takeaway / Win / Learning", color = TextMuted, fontSize = 12.sp) },
                    placeholder = { Text("What went well today? What will you do better tomorrow?", color = TextMuted.copy(alpha = 0.6f)) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonViolet,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSaveReflection(reflectionNote)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonViolet,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Debrief", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebriefCard(
    title: String,
    value: String,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "$emoji $title",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
