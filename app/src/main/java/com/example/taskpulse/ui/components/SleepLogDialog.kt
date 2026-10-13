package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun SleepLogDialog(
    initialHours: Float = 7.5f,
    initialQuality: Int = 4,
    initialNotes: String = "",
    initialDate: String = com.example.taskpulse.util.DateUtils.todayDateString,
    onDismiss: () -> Unit,
    onSubmit: (hours: Float, quality: Int, notes: String, bedtime: String, wakeTime: String, dateString: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var selectedDate by remember { mutableStateOf(initialDate) }

    // Bedtime slider: 8:00 PM (1200 mins) to 4:00 AM (1680 mins) in 15m increments
    // Default 11:15 PM = 1395 mins
    var bedtimeMinutes by remember { mutableIntStateOf(1395) }
    // Wake time slider: 4:00 AM (240 mins) to 1:00 PM (780 mins) in 15m increments
    // Default 6:45 AM = 405 mins
    var wakeMinutes by remember { mutableIntStateOf(405) }

    // Helper functions for time conversion
    fun minutesToFormattedTime(totalMinutes: Int): String {
        val normalized = ((totalMinutes % 1440) + 1440) % 1440
        val hours24 = normalized / 60
        val mins = normalized % 60
        val ampm = if (hours24 >= 12) "PM" else "AM"
        val hours12 = if (hours24 % 12 == 0) 12 else hours24 % 12
        return "%d:%02d %s".format(hours12, mins, ampm)
    }

    fun computeDuration(bed: Int, wake: Int): Float {
        val bedNorm = ((bed % 1440) + 1440) % 1440
        val wakeNorm = ((wake % 1440) + 1440) % 1440
        val durationMins = if (wakeNorm >= bedNorm) {
            wakeNorm - bedNorm
        } else {
            (1440 - bedNorm) + wakeNorm
        }
        return Math.round((durationMins / 60.0f) * 10.0f) / 10.0f
    }

    var sleepHours by remember { mutableFloatStateOf(computeDuration(bedtimeMinutes, wakeMinutes)) }
    var qualityRating by remember { mutableIntStateOf(initialQuality.coerceIn(1, 5)) }
    var notes by remember { mutableStateOf(initialNotes) }

    val formattedBedtime = minutesToFormattedTime(bedtimeMinutes)
    val formattedWakeTime = minutesToFormattedTime(wakeMinutes)

    // Calculate approximate 90m sleep cycles
    val cycles = (sleepHours / 1.5f).toInt()
    val remainderMins = ((sleepHours % 1.5f) * 60).toInt()

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
                            text = "Sleep & Recovery Log",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Slide bedtime & wake time to auto-calculate duration",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DATE SELECTOR SECTION
                Text(
                    text = "CORRESPONDING SLEEP NIGHT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dateOptions = com.example.taskpulse.util.DateUtils.getRecentDayOptions()
                    dateOptions.forEach { (dateVal, label) ->
                        val isDateSelected = selectedDate == dateVal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDateSelected) AmberGlow.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isDateSelected) Modifier.border(1.dp, AmberGlow, RoundedCornerShape(8.dp))
                                    else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedDate = dateVal
                                }
                                .padding(vertical = 7.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDateSelected) AmberGlow else TextPrimary
                                )
                                Text(
                                    text = com.example.taskpulse.util.DateUtils.formatDisplayDate(dateVal),
                                    fontSize = 9.sp,
                                    color = if (isDateSelected) AmberGlow.copy(alpha = 0.8f) else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Calculated Duration Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceVariant)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "CALCULATED DURATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${"%.1f".format(sleepHours)}h",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${sleepHours.toInt()}h ${Math.round((sleepHours % 1f) * 60)}m)",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Cycle count pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonViolet.copy(alpha = 0.2f))
                                .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "~$cycles Cycles",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonViolet
                                )
                                Text(
                                    text = if (sleepHours >= 7.0f) "Optimal Rest" else "Sleep Debt",
                                    fontSize = 9.sp,
                                    color = if (sleepHours >= 7.0f) EmeraldGreen else AmberGlow
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bedtime Slider Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛌", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TIME WENT TO SLEEP (BEDTIME)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = formattedBedtime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonViolet
                    )
                }

                Slider(
                    value = bedtimeMinutes.toFloat(),
                    onValueChange = {
                        val snapped = (Math.round(it / 15.0f) * 15).toInt()
                        if (snapped != bedtimeMinutes) {
                            bedtimeMinutes = snapped
                            sleepHours = computeDuration(bedtimeMinutes, wakeMinutes)
                        }
                    },
                    valueRange = 1200f..1680f, // 8:00 PM to 4:00 AM
                    steps = 31, // (480 / 15) - 1 = 31 steps (every 15 mins)
                    colors = SliderDefaults.colors(
                        thumbColor = NeonViolet,
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = GlassSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "8:00 PM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "11:00 PM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "1:00 AM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "4:00 AM", fontSize = 10.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wake Time Slider Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏰", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TIME WOKE UP (WAKE TIME)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = formattedWakeTime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow
                    )
                }

                Slider(
                    value = wakeMinutes.toFloat(),
                    onValueChange = {
                        val snapped = (Math.round(it / 15.0f) * 15).toInt()
                        if (snapped != wakeMinutes) {
                            wakeMinutes = snapped
                            sleepHours = computeDuration(bedtimeMinutes, wakeMinutes)
                        }
                    },
                    valueRange = 240f..780f, // 4:00 AM to 1:00 PM
                    steps = 35, // (540 / 15) - 1 = 35 steps (every 15 mins)
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGlow,
                        activeTrackColor = AmberGlow,
                        inactiveTrackColor = GlassSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "4:00 AM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "7:00 AM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "10:00 AM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "1:00 PM", fontSize = 10.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Presets (Snaps bedtime & wake time to classic healthy targets)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Triple(6.0f, 1440, 360),   // 12:00 AM to 6:00 AM (6.0h)
                        Triple(7.0f, 1410, 390),   // 11:30 PM to 6:30 AM (7.0h)
                        Triple(7.5f, 1395, 405),   // 11:15 PM to 6:45 AM (7.5h)
                        Triple(8.0f, 1380, 420),   // 11:00 PM to 7:00 AM (8.0h)
                        Triple(8.5f, 1365, 435)    // 10:45 PM to 7:15 AM (8.5h)
                    ).forEach { (presetHours, bed, wake) ->
                        val isSelected = Math.abs(sleepHours - presetHours) < 0.1f
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    bedtimeMinutes = bed
                                    wakeMinutes = wake
                                    sleepHours = presetHours
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "${presetHours.toInt()}h" + if (presetHours % 1f != 0f) ".5" else "",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sleep Quality Rating (1-5 Stars)
                val qualityLabel = when (qualityRating) {
                    1 -> "Poor / Disrupted"
                    2 -> "Fair / Restless"
                    3 -> "Average"
                    4 -> "Good & Restful"
                    5 -> "Excellent / Deep"
                    else -> ""
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SLEEP QUALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = qualityLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberGlow
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (star in 1..5) {
                        val isFilled = star <= qualityRating
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFilled) AmberGlow.copy(alpha = 0.2f) else GlassSurfaceVariant)
                                .then(
                                    if (star == qualityRating) Modifier.border(1.5.dp, AmberGlow, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    qualityRating = star
                                }
                        ) {
                            Text(
                                text = if (isFilled) "★" else "☆",
                                fontSize = 22.sp,
                                color = if (isFilled) AmberGlow else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Dreams, caffeine timing, room temperature)", color = TextMuted, fontSize = 11.sp) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = ElectricCyan,
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
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSubmit(sleepHours, qualityRating, notes, formattedBedtime, formattedWakeTime, selectedDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Sleep Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SleepLogDialog(
    initialHours: Float = 7.5f,
    initialQuality: Int = 4,
    initialNotes: String = "",
    onDismiss: () -> Unit,
    onSubmit: (hours: Float, quality: Int, notes: String, bedtime: String, wakeTime: String) -> Unit
) {
    SleepLogDialog(
        initialHours = initialHours,
        initialQuality = initialQuality,
        initialNotes = initialNotes,
        onDismiss = onDismiss,
        onSubmit = { hours, quality, notes, bedtime, wakeTime, _ ->
            onSubmit(hours, quality, notes, bedtime, wakeTime)
        }
    )
}

