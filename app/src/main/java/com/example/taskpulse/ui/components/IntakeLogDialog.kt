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
import com.example.taskpulse.data.model.IntakeType
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

@Composable
fun IntakeLogDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: IntakeType, amount: Float, unit: String, caffeineMg: Int, timeString: String, hourOfDay: Int, notes: String, dateString: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val now = Calendar.getInstance()
    val currentTotalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val snappedInitialMinutes = (Math.round(currentTotalMinutes / 15.0f) * 15).coerceIn(360, 1425)

    var selectedDate by remember { mutableStateOf(com.example.taskpulse.util.DateUtils.todayDateString) }
    var selectedType by remember { mutableStateOf(IntakeType.COFFEE) }
    var amount by remember { mutableFloatStateOf(1.0f) }
    var caffeineMg by remember { mutableIntStateOf(selectedType.defaultCaffeineMg) }
    var timeMinutes by remember { mutableIntStateOf(snappedInitialMinutes) }
    var notes by remember { mutableStateOf("") }

    fun minutesToFormatted(totalMinutes: Int): String {
        val normalized = ((totalMinutes % 1440) + 1440) % 1440
        val h = normalized / 60
        val m = normalized % 60
        val ampm = if (h >= 12) "PM" else "AM"
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(h12, m, ampm)
    }

    val hourOfDay = (timeMinutes / 60).coerceIn(0, 23)
    val formattedTime = minutesToFormatted(timeMinutes)
    val isLateCaffeine = caffeineMg > 0 && hourOfDay >= 14

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
                    Text(text = "🥤", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Log Drink & Caffeine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Track intake times to correlate with sleep & focus",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DATE SELECTOR SECTION
                Text(
                    text = "DATE LOGGED",
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
                                    text = com.example.taskpulse.util.DateUtils.formatDisplayDate(dateVal),
                                    fontSize = 9.sp,
                                    color = if (isDateSelected) ElectricCyan.copy(alpha = 0.8f) else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Drink Type Grid
                Text(
                    text = "SELECT BEVERAGE TYPE",
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
                    listOf(
                        IntakeType.COFFEE,
                        IntakeType.ENERGY_DRINK,
                        IntakeType.WATER
                    ).forEach { type ->
                        val isSelected = selectedType == type
                        val activeColor = when (type) {
                            IntakeType.COFFEE -> AmberGlow
                            IntakeType.ENERGY_DRINK -> FlameRose
                            IntakeType.WATER -> ElectricCyan
                            else -> NeonViolet
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, activeColor, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedType = type
                                    caffeineMg = type.defaultCaffeineMg
                                    amount = if (type == IntakeType.WATER) 500f else 1.0f
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = type.icon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = type.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeColor else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        IntakeType.TEA,
                        IntakeType.SODA,
                        IntakeType.OTHER
                    ).forEach { type ->
                        val isSelected = selectedType == type
                        val activeColor = when (type) {
                            IntakeType.TEA -> EmeraldGreen
                            IntakeType.SODA -> NeonViolet
                            else -> ElectricCyan
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else GlassSurfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, activeColor, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedType = type
                                    caffeineMg = type.defaultCaffeineMg
                                    amount = 1.0f
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = type.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = type.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeColor else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Logged Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIME CONSUMED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLateCaffeine) FlameRose else ElectricCyan
                    )
                }

                Slider(
                    value = timeMinutes.toFloat(),
                    onValueChange = {
                        val snapped = (Math.round(it / 15.0f) * 15).toInt()
                        if (snapped != timeMinutes) {
                            timeMinutes = snapped
                        }
                    },
                    valueRange = 360f..1410f, // 6:00 AM to 11:30 PM
                    steps = 69, // 15 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = if (isLateCaffeine) FlameRose else ElectricCyan,
                        activeTrackColor = if (isLateCaffeine) FlameRose else ElectricCyan,
                        inactiveTrackColor = GlassSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "6:00 AM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "12:00 PM", fontSize = 10.sp, color = TextMuted)
                    Text(text = "2:00 PM (Cutoff)", fontSize = 10.sp, color = FlameRose)
                    Text(text = "11:30 PM", fontSize = 10.sp, color = TextMuted)
                }

                if (isLateCaffeine) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FlameRose.copy(alpha = 0.15f))
                            .border(1.dp, FlameRose.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚠️ Intake logged after 2:00 PM — Caffeine half-life may delay sleep & reduce deep REM cycles!",
                            fontSize = 11.sp,
                            color = FlameRose,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity & Caffeine Steppers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Amount Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "PORTION / AMOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${if (amount % 1f == 0f) amount.toInt().toString() else "%.1f".format(amount)} ${if (selectedType == IntakeType.WATER) "ml" else selectedType.defaultUnit}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GlassBorder)
                                            .clickable {
                                                if (selectedType == IntakeType.WATER) {
                                                    amount = (amount - 250f).coerceAtLeast(250f)
                                                } else {
                                                    amount = (amount - 1.0f).coerceAtLeast(1.0f)
                                                    caffeineMg = (selectedType.defaultCaffeineMg * amount).toInt()
                                                }
                                            }
                                    ) {
                                        Text(text = "-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ElectricCyan.copy(alpha = 0.3f))
                                            .clickable {
                                                if (selectedType == IntakeType.WATER) {
                                                    amount += 250f
                                                } else {
                                                    amount += 1.0f
                                                    caffeineMg = (selectedType.defaultCaffeineMg * amount).toInt()
                                                }
                                            }
                                    ) {
                                        Text(text = "+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                    }
                                }
                            }
                        }
                    }

                    // Caffeine Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "EST. CAFFEINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$caffeineMg mg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caffeineMg > 0) AmberGlow else ElectricCyan
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GlassBorder)
                                            .clickable {
                                                caffeineMg = (caffeineMg - 20).coerceAtLeast(0)
                                            }
                                    ) {
                                        Text(text = "-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AmberGlow.copy(alpha = 0.3f))
                                            .clickable {
                                                caffeineMg = (caffeineMg + 20).coerceAtMost(600)
                                            }
                                    ) {
                                        Text(text = "+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Brand / Details (e.g. Monster Mango, Cold Brew, Matcha)", color = TextMuted, fontSize = 11.sp) },
                    singleLine = true,
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
                            val unitStr = if (selectedType == IntakeType.WATER) "ml" else selectedType.defaultUnit
                            onSubmit(
                                selectedType,
                                amount,
                                unitStr,
                                caffeineMg,
                                formattedTime,
                                hourOfDay,
                                notes,
                                selectedDate
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLateCaffeine) FlameRose else ElectricCyan,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Drink Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun IntakeLogDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: IntakeType, amount: Float, unit: String, caffeineMg: Int, timeString: String, hourOfDay: Int, notes: String) -> Unit
) {
    IntakeLogDialog(
        onDismiss = onDismiss,
        onSubmit = { type, amount, unit, caffeineMg, timeString, hourOfDay, notes, _ ->
            onSubmit(type, amount, unit, caffeineMg, timeString, hourOfDay, notes)
        }
    )
}

