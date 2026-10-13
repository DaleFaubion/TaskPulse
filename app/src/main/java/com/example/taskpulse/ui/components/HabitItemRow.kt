package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.model.Habit
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary

import androidx.compose.foundation.border
import com.example.taskpulse.util.DateUtils

@Composable
fun HabitItemRow(
    habit: Habit,
    targetDate: String = DateUtils.todayDateString,
    onToggle: () -> Unit,
    onToggleDay: (Int) -> Unit = {},
    onToggleDate: (String) -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = remember(habit.completedDates) { DateUtils.getCurrentWeekDates() }
    val isDoneTargetDate = targetDate in habit.completedDates

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        cornerRadius = 14.dp,
        contentPadding = 10.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Flame Icon & Streak Counter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDoneTargetDate) FlameRose.copy(alpha = 0.25f)
                        else GlassSurfaceVariant
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥", fontSize = 13.sp)
                    Text(
                        text = "${habit.streakCount}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDoneTargetDate) FlameRose else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    val milestoneBadge = when {
                        habit.streakCount >= 30 -> "💎 30d"
                        habit.streakCount >= 14 -> "🏆 14d"
                        habit.streakCount >= 7 -> "🔥 7d"
                        else -> null
                    }
                    if (milestoneBadge != null) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FlameRose.copy(alpha = 0.2f))
                                .border(1.dp, FlameRose.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = milestoneBadge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlameRose
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = habit.category,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Best: ${habit.bestStreak}d",
                        fontSize = 10.sp,
                        color = ElectricCyan
                    )
                    val daysHit = habit.completedDates.count { dateStr -> weekDays.any { it.dateString == dateStr } }
                    Text(
                        text = "Goal: $daysHit/${habit.targetDaysPerWeek}d",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (daysHit >= habit.targetDaysPerWeek) EmeraldGreen else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Current Calendar Week (Mon - Sun) with Active Today Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEachIndexed { idx, dayInfo ->
                        val isDayDone = dayInfo.dateString in habit.completedDates
                        val isToday = dayInfo.isToday
                        val isFuture = dayInfo.isFuture

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(22.dp)
                                .height(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isDayDone -> FlameRose.copy(alpha = 0.95f)
                                        isToday -> ElectricCyan.copy(alpha = 0.2f)
                                        isFuture -> GlassSurfaceVariant.copy(alpha = 0.4f)
                                        else -> GlassSurfaceVariant
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(6.dp))
                                    else if (isDayDone) Modifier.border(1.dp, FlameRose, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .clickable {
                                    onToggleDate(dayInfo.dateString)
                                    onToggleDay(idx)
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayInfo.dayLabel,
                                    fontSize = 8.sp,
                                    fontWeight = if (isDayDone || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = when {
                                        isDayDone -> TextPrimary
                                        isToday -> ElectricCyan
                                        isFuture -> TextMuted.copy(alpha = 0.5f)
                                        else -> TextMuted
                                    }
                                )
                                Text(
                                    text = dayInfo.dayNumber,
                                    fontSize = 8.sp,
                                    fontWeight = if (isDayDone || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isDayDone -> TextPrimary
                                        isToday -> ElectricCyan
                                        isFuture -> TextMuted.copy(alpha = 0.5f)
                                        else -> TextMuted.copy(alpha = 0.8f)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Check-in Action Button
            Button(
                onClick = { onToggleDate(targetDate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDoneTargetDate) EmeraldGreen else NeonViolet,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(28.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isDoneTargetDate) "Done ✓" else "Check In",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Text(
                    text = "✕",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}
