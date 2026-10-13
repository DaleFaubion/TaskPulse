package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.taskpulse.data.model.TimeClassification
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
import java.util.Calendar
import com.example.taskpulse.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HourlyCheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (timeSlot: String, summary: String, rating: Int, classification: TimeClassification, category: String, dateString: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val now = remember { Calendar.getInstance() }
    val currentHour = remember { now.get(Calendar.HOUR_OF_DAY) }

    var selectedDate by remember { mutableStateOf(DateUtils.todayDateString) }
    var startHour by remember { mutableIntStateOf((currentHour - 1 + 24) % 24) }
    var endHour by remember { mutableIntStateOf(if (currentHour == 0) 1 else currentHour) }

    fun formatHour(h: Int): String {
        val num = if (h == 0) 12 else if (h > 12) h - 12 else h
        val ampm = if (h < 12) "AM" else "PM"
        return "$num:00 $ampm"
    }

    fun computeSlot(s: Int, e: Int): String {
        return "${formatHour(s)} - ${formatHour(e)}"
    }

    var timeSlot by remember { mutableStateOf(computeSlot(startHour, endHour)) }
    var summary by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(4) }
    var selectedClassification by remember { mutableStateOf(TimeClassification.PRODUCTIVE) }
    var selectedCategory by remember { mutableStateOf("Work") }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("taskpulse_activity_tags", Context.MODE_PRIVATE) }
    val defaultCategories = remember {
        listOf("Work", "Coding", "Meeting", "Learning", "Break", "Fitness", "Social", "Leisure")
    }
    var customTags by remember {
        val saved = prefs.getStringSet("custom_tags", emptySet()) ?: emptySet()
        mutableStateOf(saved.toList().sorted())
    }
    var isAddingCustomTag by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }

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
                    Text(text = "⏱️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Hourly Check-In",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Log and reflect on your time blocks",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DATE SELECTOR SECTION
                Text(
                    text = "CORRESPONDING DAY",
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
                    val dateOptions = DateUtils.getRecentDayOptions()
                    dateOptions.forEach { (dateVal, label) ->
                        val isDateSelected = selectedDate == dateVal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDateSelected) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isDateSelected) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
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
                                    fontSize = 11.sp,
                                    fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDateSelected) ElectricCyan else TextPrimary
                                )
                                Text(
                                    text = DateUtils.formatDisplayDate(dateVal),
                                    fontSize = 9.sp,
                                    color = if (isDateSelected) ElectricCyan.copy(alpha = 0.8f) else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // EASY HOUR OR HOURS LOGGING SECTION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HOURS & TIME SLOT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    val hourSpan = ((endHour - startHour + 24) % 24).let { if (it == 0) 1 else it }
                    Text(
                        text = "Duration: $hourSpan ${if (hourSpan == 1) "hr" else "hrs"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick presets: Past 1h, Past 2h, Morning, Afternoon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        "Past 1h" to Pair((currentHour - 1 + 24) % 24, currentHour),
                        "Past 2h" to Pair((currentHour - 2 + 24) % 24, currentHour),
                        "9-11 AM" to Pair(9, 11),
                        "1-3 PM" to Pair(13, 15),
                        "3-5 PM" to Pair(15, 17)
                    )
                    presets.forEach { (label, range) ->
                        val isPresetActive = startHour == range.first && endHour == range.second
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPresetActive) NeonViolet.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isPresetActive) Modifier.border(1.dp, NeonViolet, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    startHour = range.first
                                    endHour = range.second
                                    timeSlot = computeSlot(range.first, range.second)
                                }
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPresetActive) NeonViolet else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Visual Hour Steppers for Start Hour and End Hour
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Start Hour Stepper
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Start Hour", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassSurfaceVariant)
                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GlassBorder)
                                    .clickable {
                                        startHour = (startHour - 1 + 24) % 24
                                        if (startHour == endHour) endHour = (startHour + 1) % 24
                                        timeSlot = computeSlot(startHour, endHour)
                                    }
                            ) {
                                Text(text = "−", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                text = formatHour(startHour),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricCyan.copy(alpha = 0.25f))
                                    .clickable {
                                        startHour = (startHour + 1) % 24
                                        if (startHour == endHour) endHour = (startHour + 1) % 24
                                        timeSlot = computeSlot(startHour, endHour)
                                    }
                            ) {
                                Text(text = "+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                    }

                    // End Hour Stepper
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "End Hour", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassSurfaceVariant)
                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GlassBorder)
                                    .clickable {
                                        endHour = (endHour - 1 + 24) % 24
                                        if (startHour == endHour) startHour = (endHour - 1 + 24) % 24
                                        timeSlot = computeSlot(startHour, endHour)
                                    }
                            ) {
                                Text(text = "−", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                text = formatHour(endHour),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricCyan.copy(alpha = 0.25f))
                                    .clickable {
                                        endHour = (endHour + 1) % 24
                                        if (startHour == endHour) endHour = (startHour + 1) % 24
                                        timeSlot = computeSlot(startHour, endHour)
                                    }
                            ) {
                                Text(text = "+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Time Slot Field (Synced with Steppers)
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Selected Time Slot", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Field
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("What did you do this hour?", color = TextMuted) },
                    placeholder = { Text("e.g. Built navigation flow, fixed bug...", color = TextMuted.copy(alpha = 0.6f)) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonViolet,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Classification Section
                Text(
                    text = "CLASSIFICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimeClassification.entries.forEach { classification ->
                        val isSelected = selectedClassification == classification
                        val (chipColor, chipBg) = when (classification) {
                            TimeClassification.PRODUCTIVE -> Pair(EmeraldGreen, EmeraldGreen.copy(alpha = 0.25f))
                            TimeClassification.RECHARGE -> Pair(ElectricCyan, ElectricCyan.copy(alpha = 0.25f))
                            TimeClassification.NEUTRAL -> Pair(AmberGlow, AmberGlow.copy(alpha = 0.25f))
                            TimeClassification.WASTED -> Pair(FlameRose, FlameRose.copy(alpha = 0.25f))
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) chipBg else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, chipColor, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedClassification = classification
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = classification.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = classification.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) chipColor else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Productivity Rating (1-5 Stars)
                Text(
                    text = "PRODUCTIVITY RATING: $rating / 5",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGlow,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (star in 1..5) {
                        val isFilled = star <= rating
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFilled) AmberGlow.copy(alpha = 0.2f) else GlassSurfaceVariant)
                                .then(
                                    if (star == rating) Modifier.border(1.5.dp, AmberGlow, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    rating = star
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

                Spacer(modifier = Modifier.height(14.dp))

                // Category Tag Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ACTIVITY TAG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Selected: $selectedCategory",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Default Tags
                    defaultCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.dp, NeonViolet, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextMuted
                            )
                        }
                    }

                    // User's Custom Tags
                    customTags.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricCyan else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "×",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val updated = customTags - cat
                                            customTags = updated
                                            prefs.edit().putStringSet("custom_tags", updated.toSet()).apply()
                                            if (selectedCategory == cat) selectedCategory = "Work"
                                        }
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    // + Custom Tag button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAddingCustomTag) AmberGlow.copy(alpha = 0.2f) else GlassSurfaceVariant)
                            .then(
                                if (isAddingCustomTag) Modifier.border(1.dp, AmberGlow, RoundedCornerShape(8.dp))
                                else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isAddingCustomTag = !isAddingCustomTag
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAddingCustomTag) "Close" else "+ Custom",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAddingCustomTag) AmberGlow else TextSecondary
                        )
                    }
                }

                // Inline Custom Tag Input Field
                if (isAddingCustomTag) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customTagInput,
                            onValueChange = { customTagInput = it },
                            placeholder = { Text("e.g. Reading, Chores, Gaming", fontSize = 12.sp, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGlow,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = customTagInput.trim()
                                if (trimmed.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (trimmed !in defaultCategories && trimmed !in customTags) {
                                        val updated = (customTags + trimmed).sorted()
                                        customTags = updated
                                        prefs.edit().putStringSet("custom_tags", updated.toSet()).apply()
                                    }
                                    selectedCategory = trimmed
                                    customTagInput = ""
                                    isAddingCustomTag = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGlow,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dialog Action Buttons
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
                            if (summary.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSubmit(timeSlot, summary, rating, selectedClassification, selectedCategory, selectedDate)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Check-In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HourlyCheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (timeSlot: String, summary: String, rating: Int, classification: TimeClassification, category: String) -> Unit
) {
    HourlyCheckInDialog(
        onDismiss = onDismiss,
        onSubmit = { timeSlot, summary, rating, classification, category, _ ->
            onSubmit(timeSlot, summary, rating, classification, category)
        }
    )
}

