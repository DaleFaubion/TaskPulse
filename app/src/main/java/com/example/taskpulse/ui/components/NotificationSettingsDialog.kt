package com.example.taskpulse.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.taskpulse.notifications.ReminderConfig
import com.example.taskpulse.notifications.ReminderManager
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassBorder
import com.example.taskpulse.theme.GlassSurface
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.MidnightBackground
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit,
    onConfigSaved: (ReminderConfig) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val currentConfig = remember { ReminderManager.getReminderConfig(context) }
    var isEnabled by remember { mutableStateOf(currentConfig.isEnabled) }
    var selectedIntervalHours by remember { mutableIntStateOf(currentConfig.intervalHours) }
    var quietHoursEnabled by remember { mutableStateOf(currentConfig.quietHoursEnabled) }

    // Android 13+ Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please enable notifications in system settings", Toast.LENGTH_LONG).show()
        }
    }

    val requestPermissionIfNeeded = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val presetIntervals = listOf(
        Pair(1, "Every 1 hr"),
        Pair(2, "Every 2 hrs"),
        Pair(3, "Every 3 hrs"),
        Pair(4, "Every 4 hrs"),
        Pair(6, "Every 6 hrs"),
        Pair(8, "Every 8 hrs"),
        Pair(12, "Every 12 hrs"),
        Pair(24, "Daily (24h)")
    )

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reminder Settings",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Custom check-in intervals",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isEnabled = it
                            if (it) requestPermissionIfNeeded()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = ElectricCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GlassSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEnabled) {
                    // Preset Intervals Section
                    Text(
                        text = "CHOOSE INTERVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetIntervals.forEach { (hours, label) ->
                            val isSelected = selectedIntervalHours == hours
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) ElectricCyan.copy(alpha = 0.25f)
                                        else GlassSurfaceVariant
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.5.dp, ElectricCyan, RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedIntervalHours = hours
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ElectricCyan else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Slider Stepper
                    Text(
                        text = "CUSTOM INTERVAL: EVERY $selectedIntervalHours ${if (selectedIntervalHours == 1) "HOUR" else "HOURS"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.5.sp
                    )
                    Slider(
                        value = selectedIntervalHours.toFloat(),
                        onValueChange = {
                            selectedIntervalHours = it.roundToInt()
                        },
                        valueRange = 1f..24f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = GlassSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quiet Hours Filter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                quietHoursEnabled = !quietHoursEnabled
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Hours Only (8 AM – 10 PM)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Mutes alerts overnight so you can sleep",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = quietHoursEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                quietHoursEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = NeonViolet,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = GlassSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Alert Button
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            requestPermissionIfNeeded()
                            ReminderManager.triggerTestNotification(
                                context = context,
                                title = "TaskPulse • Interval Reminder 🔥",
                                message = "Custom alert test (${if (selectedIntervalHours == 1) "Every hour" else "Every $selectedIntervalHours hours"}). Tap to check in!"
                            )
                            Toast.makeText(context, "Test alert sent to status bar! 🔔", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Send Test Notification Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            requestPermissionIfNeeded()
                            ReminderManager.triggerWindDownTest(context)
                            Toast.makeText(context, "🌙 Wind-down test notification dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FlameRose
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FlameRose.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🌙 Test Evening Wind-Down Alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlameRose
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurfaceVariant)
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Reminders are currently turned off. Turn the switch on above to set up hourly or periodic check-in alerts.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            val newConfig = ReminderConfig(
                                isEnabled = isEnabled,
                                intervalHours = selectedIntervalHours,
                                startHour = 8,
                                endHour = 22,
                                quietHoursEnabled = quietHoursEnabled
                            )
                            ReminderManager.saveReminderConfig(context, newConfig)
                            onConfigSaved(newConfig)
                            val msg = if (isEnabled) {
                                "Reminders set: ${ReminderManager.getSummaryText(newConfig)}!"
                            } else {
                                "Reminders turned off"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = MidnightBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
