package com.example.taskpulse.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.taskpulse.widget.QuickLogWidgetProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskpulse.data.model.Task
import com.example.taskpulse.notifications.ReminderManager
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassBorder
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.MidnightBackground
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.ui.components.AddDialog
import com.example.taskpulse.ui.components.DailyDebriefDialog
import com.example.taskpulse.ui.components.DataVisualizationScreen
import com.example.taskpulse.ui.components.GlassCard
import com.example.taskpulse.ui.components.HabitItemRow
import com.example.taskpulse.ui.components.HourlyCheckInDialog
import com.example.taskpulse.ui.components.HourlyCheckInRow
import com.example.taskpulse.ui.components.IntakeItemRow
import com.example.taskpulse.ui.components.IntakeLogDialog
import com.example.taskpulse.ui.components.NotificationSettingsDialog
import com.example.taskpulse.ui.components.ProductivityBreakdownCard
import com.example.taskpulse.ui.components.ProgressRing
import com.example.taskpulse.ui.components.SleepLogDialog
import com.example.taskpulse.ui.components.TaskItemRow
import com.example.taskpulse.util.DateUtils

@Composable
fun TaskPulseAppScreen(
    initialOpenCheckIn: Boolean = false,
    viewModel: TaskPulseViewModel = viewModel(factory = TaskPulseViewModel.factory(LocalContext.current))
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val sleepLogs by viewModel.sleepLogs.collectAsState()
    val intakes by viewModel.intakes.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()

    var currentScreenMode by remember { mutableStateOf(0) } // 0 = Daily Tracker, 1 = Data Visualization
    var selectedFilterTab by remember { mutableStateOf(0) } // 0 = All, 1 = Tasks, 2 = Habits, 3 = Hourly, 4 = Drinks
    var selectedDayFilter by remember { mutableStateOf(DateUtils.todayDateString) } // Active day matches calendar today
    var showAddDialog by remember { mutableStateOf(false) }
    var showHourlyCheckInDialog by remember { mutableStateOf(initialOpenCheckIn) }
    var showIntakeLogDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showSleepLogDialog by remember { mutableStateOf(false) }
    var showDebriefDialog by remember { mutableStateOf(false) }
    var reminderConfig by remember { mutableStateOf(ReminderManager.getReminderConfig(context)) }
    var sortByUrgency by remember { mutableStateOf(false) }

    fun exportData() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val csv = viewModel.generateExportCsv()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_SUBJECT, "TaskPulse Productivity Data Export")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export TaskPulse Data")
        context.startActivity(shareIntent)
    }

    Scaffold(
        containerColor = MidnightBackground,
        floatingActionButton = {
            if (currentScreenMode == 0) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddDialog = true
                    },
                    containerColor = NeonViolet,
                    contentColor = TextPrimary,
                    shape = CircleShape,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TaskPulse",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "⚡", fontSize = 16.sp)
                    }
                    Text(
                        text = "📅 ${DateUtils.todayHeaderString}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                // Header Action Chips (Export, Debrief, Reminders)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export CSV
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GlassSurfaceVariant)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .clickable { exportData() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(text = "📤 Export", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    // Daily Debrief
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonViolet.copy(alpha = 0.2f))
                            .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDebriefDialog = true
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(text = "📊 Debrief", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonViolet)
                    }

                    // Reminders Pill
                    val summaryText = ReminderManager.getShortSummaryText(reminderConfig)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (reminderConfig.isEnabled) ElectricCyan.copy(alpha = 0.15f) else GlassSurfaceVariant)
                            .border(
                                1.dp,
                                if (reminderConfig.isEnabled) ElectricCyan.copy(alpha = 0.5f) else GlassBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showNotificationSettingsDialog = true
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(text = if (reminderConfig.isEnabled) "🔔" else "🔕", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = summaryText,
                            fontSize = 10.sp,
                            color = if (reminderConfig.isEnabled) ElectricCyan else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Screen Mode Switcher: Daily Tracker vs Data Visualization
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentScreenMode == 0) NeonViolet.copy(alpha = 0.25f) else GlassSurfaceVariant)
                        .then(
                            if (currentScreenMode == 0) Modifier.border(1.2.dp, NeonViolet, RoundedCornerShape(10.dp))
                            else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentScreenMode = 0
                        }
                        .padding(vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Daily Tracker",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreenMode == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentScreenMode == 0) TextPrimary else TextMuted
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentScreenMode == 1) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                        .then(
                            if (currentScreenMode == 1) Modifier.border(1.2.dp, ElectricCyan, RoundedCornerShape(10.dp))
                            else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentScreenMode = 1
                        }
                        .padding(vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📈", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Data Visualization",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreenMode == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentScreenMode == 1) TextPrimary else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (currentScreenMode == 1) {
                // Dedicated Data Visualization & Cross-Correlations Screen
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    DataVisualizationScreen(
                        dashboardState = dashboardState,
                        sleepLogs = sleepLogs,
                        checkIns = checkIns,
                        intakes = intakes,
                        tasks = tasks,
                        onOpenSleepDialog = { showSleepLogDialog = true },
                        onOpenIntakeDialog = { showIntakeLogDialog = true }
                    )
                }
            } else {
                // Dynamic Data based on selectedDayFilter
                val isViewingToday = selectedDayFilter == DateUtils.todayDateString || selectedDayFilter == "all"
                val targetDate = if (selectedDayFilter == "all") DateUtils.todayDateString else selectedDayFilter

                val dayHabitsHit = habits.count { targetDate in it.completedDates }
                val dayDoneTasks = tasks.count { (it.dueDate == targetDate || (isViewingToday && it.dueDate == "Today")) && it.isCompleted }
                val dayTotalTasks = tasks.count { it.dueDate == targetDate || (isViewingToday && it.dueDate == "Today") }
                val dayTotalHabits = habits.size

                val dayProgressFraction = if (dayTotalTasks + dayTotalHabits > 0) {
                    (dayDoneTasks + dayHabitsHit).toFloat() / (dayTotalTasks + dayTotalHabits).toFloat()
                } else 0f

                val daySleep = sleepLogs.firstOrNull { it.dateString == targetDate }
                val daySleepHrs = daySleep?.hours ?: 0.0f
                val daySleepQual = daySleep?.qualityRating ?: 0

                val dayIntakes = intakes.filter { if (isViewingToday) it.isActuallyToday else it.dateString == targetDate }
                val dayCoffeeCount = dayIntakes.filter { it.type == IntakeType.COFFEE }.map { it.amount }.sum().toInt() / 250
                val dayEnergyCount = dayIntakes.count { it.type == IntakeType.ENERGY_DRINK }
                val dayWaterMl = dayIntakes.filter { it.type == IntakeType.WATER }.map { it.amount }.sum().toInt()
                val dayTotalIntakes = dayIntakes.size

                // Dashboard Summary Hero Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = 9.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProgressRing(
                            progress = dayProgressFraction,
                            size = 64.dp,
                            strokeWidth = 6.dp,
                            percentageFontSize = 15.sp,
                            labelFontSize = 8.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isViewingToday) "Daily Overview" else "Overview (${DateUtils.formatDisplayDate(targetDate)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "🔥 ${dashboardState.activeHabitStreaks}d streak",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FlameRose
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tasks: ${dayDoneTasks}/${dayTotalTasks} Done",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "•",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Habits: ${dayHabitsHit}/${dayTotalHabits} Done",
                                    fontSize = 11.sp,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (daySleepHrs > 0) {
                                    Text(
                                        text = "🌙 ${"%.1f".format(daySleepHrs)}h (${daySleepQual}★)",
                                        fontSize = 10.sp,
                                        color = AmberGlow,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "🥤 ☕${dayCoffeeCount} ⚡${dayEnergyCount} 💧${dayWaterMl}ml",
                                    fontSize = 10.sp,
                                    color = if (dayTotalIntakes > 0) AmberGlow else TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Actions Strip: Sleep, Hourly Pulse, and Drinks Log
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sleep Log Quick Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberGlow.copy(alpha = 0.12f))
                            .border(1.dp, AmberGlow.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showSleepLogDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                Text(
                                    text = if (daySleepHrs > 0) "${"%.1f".format(daySleepHrs)}h Rest" else "Sleep",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (daySleepHrs > 0) "${daySleepQual}★ Quality" else "Log rest",
                                    fontSize = 8.sp,
                                    color = AmberGlow
                                )
                            }
                        }
                    }

                    // Hourly Pulse Quick Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.12f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showHourlyCheckInDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏱️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                Text(
                                    text = "Hourly",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${dashboardState.totalCheckIns} logged",
                                    fontSize = 8.sp,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }

                    // Drinks & Caffeine Quick Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FlameRose.copy(alpha = 0.12f))
                            .border(1.dp, FlameRose.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showIntakeLogDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🥤", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                Text(
                                    text = "Drinks",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${dayTotalIntakes} today • +Log",
                                    fontSize = 8.sp,
                                    color = FlameRose
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick 1-Tap Drink Logging Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberGlow.copy(alpha = 0.15f))
                            .border(1.dp, AmberGlow.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.quickLogDrink(IntakeType.COFFEE)
                                QuickLogWidgetProvider.updateAllWidgets(context)
                                val currentH = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                if (currentH >= 14) {
                                    val remaining = (95 * Math.pow(0.5, ((23.5 - currentH) / 5.5))).toInt().coerceAtLeast(1)
                                    com.example.taskpulse.notifications.ReminderManager.scheduleWindDownReminder(context, "Coffee", 95, currentH, remaining)
                                    Toast.makeText(context, "☕ Logged Coffee (95mg) • 🌙 Wind-down alert set!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Logged Coffee (95mg)", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "+☕ Coffee", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(FlameRose.copy(alpha = 0.15f))
                            .border(1.dp, FlameRose.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.quickLogDrink(IntakeType.ENERGY_DRINK)
                                QuickLogWidgetProvider.updateAllWidgets(context)
                                val currentH = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                if (currentH >= 14) {
                                    val remaining = (160 * Math.pow(0.5, ((23.5 - currentH) / 5.5))).toInt().coerceAtLeast(1)
                                    com.example.taskpulse.notifications.ReminderManager.scheduleWindDownReminder(context, "Energy Drink", 160, currentH, remaining)
                                    Toast.makeText(context, "⚡ Logged Energy Drink (160mg) • 🌙 Wind-down alert set!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Logged Energy Drink (160mg)", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "+⚡ Energy", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.quickLogDrink(IntakeType.WATER)
                                QuickLogWidgetProvider.updateAllWidgets(context)
                                Toast.makeText(context, "Logged Water (250ml)", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "+💧 Water", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GlassSurfaceVariant)
                            .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showIntakeLogDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+ Details", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

            // Filter Tabs Pill Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabTitles = listOf("All", "Tasks", "Habits", "Hourly", "Drinks")
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedFilterTab == index
                    val tabColor = when (index) {
                        1 -> FlameRose
                        2 -> NeonViolet
                        3 -> ElectricCyan
                        4 -> AmberGlow
                        else -> ElectricCyan
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) tabColor.copy(alpha = 0.22f) else GlassSurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) tabColor else GlassBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedFilterTab = index
                            }
                            .padding(vertical = 5.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Timeline Centering
            val scrollableDates = remember { DateUtils.getScrollableDayRange(14, 3) }
            val lazyRowState = rememberLazyListState()

            // Center today horizontally on load
            LaunchedEffect(Unit) {
                val todayIndex = scrollableDates.indexOfFirst { it.isToday }
                if (todayIndex >= 0) {
                    lazyRowState.scrollToItem((todayIndex - 3).coerceAtLeast(0))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMELINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GlassSurfaceVariant)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedDayFilter = "all"
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("∞ All", fontSize = 8.sp, color = TextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricCyan.copy(alpha=0.2f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedDayFilter = DateUtils.todayDateString
                                    coroutineScope.launch {
                                        val todayIndex = scrollableDates.indexOfFirst { it.isToday }
                                        if (todayIndex >= 0) {
                                            lazyRowState.animateScrollToItem((todayIndex - 3).coerceAtLeast(0))
                                        }
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ Today", fontSize = 8.sp, color = ElectricCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                LazyRow(
                    state = lazyRowState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(scrollableDates) {
                        dayInfo ->
                        val isSelected = selectedDayFilter == dayInfo.dateString
                        val isToday = dayInfo.isToday
                        val isFuture = dayInfo.isFuture

                        // Compute if anything was done on this day
                        val habitsHitOnDay = habits.count { dayInfo.dateString in it.completedDates }
                        val tasksDoneOnDay = tasks.count { (it.dueDate == dayInfo.dateString || (isToday && it.dueDate == "Today")) && it.isCompleted }
                        val hasActivity = habitsHitOnDay > 0 || tasksDoneOnDay > 0

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> ElectricCyan.copy(alpha = 0.25f)
                                        isToday -> ElectricCyan.copy(alpha = 0.12f)
                                        isFuture -> GlassSurfaceVariant.copy(alpha = 0.5f)
                                        else -> GlassSurfaceVariant
                                    }
                                )
                                .then(
                                    when {
                                        isSelected -> Modifier.border(1.2.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                        isToday -> Modifier.border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        else -> Modifier.border(0.8.dp, GlassBorder, RoundedCornerShape(8.dp))
                                    }
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedDayFilter = dayInfo.dateString
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayInfo.dayShortName.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> ElectricCyan
                                        isToday -> ElectricCyan
                                        isFuture -> TextMuted.copy(alpha = 0.5f)
                                        else -> TextMuted
                                    }
                                )
                                Text(
                                    text = dayInfo.dayNumber,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = when {
                                        isSelected -> TextPrimary
                                        isToday -> ElectricCyan
                                        isFuture -> TextSecondary.copy(alpha = 0.5f)
                                        else -> TextSecondary
                                    }
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(if (hasActivity) EmeraldGreen else if (isToday) ElectricCyan else androidx.compose.ui.graphics.Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            val displayCheckIns = remember(checkIns, selectedDayFilter) {
                if (selectedDayFilter == "all") checkIns else checkIns.filter { it.dateString == selectedDayFilter }
            }
            val displayIntakes = remember(intakes, selectedDayFilter) {
                if (selectedDayFilter == "all") intakes else intakes.filter { it.dateString == selectedDayFilter }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Lists
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Productivity Breakdown Widget (shown on Hourly or All tab)
                if (selectedFilterTab == 0 || selectedFilterTab == 3) {
                    item {
                        ProductivityBreakdownCard(
                            totalCheckIns = dashboardState.totalCheckIns,
                            productiveHours = dashboardState.productiveHours,
                            rechargeHours = dashboardState.rechargeHours,
                            neutralHours = dashboardState.neutralHours,
                            wastedHours = dashboardState.wastedHours,
                            avgRating = dashboardState.avgProductivityRating
                        )
                    }
                }

                // Habits Section
                if (selectedFilterTab == 0 || selectedFilterTab == 2) {
                    if (habits.isNotEmpty()) {
                        item {
                            Text(
                                text = "DAILY HABIT STREAKS (${habits.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                            )
                        }
                        items(habits, key = { "habit-${it.id}" }) { habit ->
                            HabitItemRow(
                                habit = habit,
                                targetDate = targetDate,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleHabit(habit.id)
                                },
                                onToggleDay = { dayIndex ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleHabitDay(habit.id, dayIndex)
                                },
                                onToggleDate = { dateStr ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleHabitDate(habit.id, dateStr)
                                },
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteHabit(habit.id)
                                }
                            )
                        }
                    }
                }

                // Tasks Section
                if (selectedFilterTab == 0 || selectedFilterTab == 1) {
                    if (tasks.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 2.dp)
                            ) {
                                Text(
                                    text = "TASKS & TODOS (${tasks.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sortByUrgency) AmberGlow.copy(alpha = 0.2f) else GlassSurfaceVariant)
                                        .border(1.dp, if (sortByUrgency) AmberGlow else GlassBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            sortByUrgency = !sortByUrgency
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (sortByUrgency) "Sort: ⚡ Urgency" else "Sort: Default",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sortByUrgency) AmberGlow else TextMuted
                                    )
                                }
                            }
                        }

                        val displayTasks = if (sortByUrgency) {
                            tasks.sortedWith(
                                compareBy<Task> { it.isCompleted }
                                    .thenBy {
                                        when {
                                            it.dueDate.contains("Overdue", ignoreCase = true) -> 0
                                            it.dueDate.contains("Today", ignoreCase = true) -> 1
                                            it.dueDate.contains("Tomorrow", ignoreCase = true) -> 2
                                            else -> 3
                                        }
                                    }
                                    .thenBy { it.priority.ordinal }
                            )
                        } else tasks

                        items(displayTasks, key = { "task-${it.id}" }) { task ->
                            TaskItemRow(
                                task = task,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleTask(task.id)
                                },
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteTask(task.id)
                                }
                            )
                        }
                    }
                }

                // Hourly Check-In List
                if (selectedFilterTab == 0 || selectedFilterTab == 3) {
                    if (displayCheckIns.isNotEmpty()) {
                        item {
                            Text(
                                text = "HOURLY PULSE ENTRIES (${displayCheckIns.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                            )
                        }
                        items(displayCheckIns, key = { "checkin-${it.id}" }) { checkIn ->
                            HourlyCheckInRow(
                                checkIn = checkIn,
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteCheckIn(checkIn.id)
                                }
                            )
                        }
                    } else if (selectedFilterTab == 3) {
                        item {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHourlyCheckInDialog = true },
                                cornerRadius = 16.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(text = "⏱️", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hourly check-ins logged yet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap here to log what you did over the past hour!",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Logged Drinks & Intake Section
                if (selectedFilterTab == 0 || selectedFilterTab == 4) {
                    if (displayIntakes.isNotEmpty()) {
                        item {
                            Text(
                                text = "LOGGED DRINKS & CAFFEINE (${displayIntakes.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                            )
                        }
                        items(displayIntakes, key = { "intake-${it.id}" }) { intake ->
                            IntakeItemRow(
                                intake = intake,
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteIntake(intake.id)
                                }
                            )
                        }
                    } else if (selectedFilterTab == 4) {
                        item {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIntakeLogDialog = true },
                                cornerRadius = 16.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(text = "☕", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No drinks logged yet today",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap here to log coffee, energy drinks, or water!",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFilterTab == 4) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        QuickLogWidgetProvider.requestPinWidget(context)
                                    },
                                cornerRadius = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "⚡", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Add Quick-Log Widget to Home",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricCyan
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "1-tap Coffee, Energy & Water logging directly from your phone screen",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ElectricCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "+ Add",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Dialogs
    if (showAddDialog) {
        AddDialog(
            onDismiss = { showAddDialog = false },
            onAddTask = { title, desc, prio, cat, due ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.addTask(title, desc, prio, cat, due)
            },
            onAddHabitDates = { title, cat, dates ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.addHabit(title, cat, dates)
            },
            onAddHabit = { title, cat, days ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.addHabit(title, cat, days)
            }
        )
    }

    if (showHourlyCheckInDialog) {
        HourlyCheckInDialog(
            onDismiss = { showHourlyCheckInDialog = false },
            onSubmit = { timeSlot, summary, rating, classification, category, dateString ->
                viewModel.addCheckIn(timeSlot, summary, rating, classification, category, dateString)
            }
        )
    }

    if (showNotificationSettingsDialog) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationSettingsDialog = false },
            onConfigSaved = { newConfig ->
                reminderConfig = newConfig
            }
        )
    }

    if (showSleepLogDialog) {
        val todaySleep = sleepLogs.firstOrNull()
        SleepLogDialog(
            initialHours = todaySleep?.hours ?: 7.5f,
            initialQuality = todaySleep?.qualityRating ?: 4,
            initialNotes = todaySleep?.notes ?: "",
            onDismiss = { showSleepLogDialog = false },
            onSubmit = { hours, quality, notes, bedtime, wakeTime, dateString ->
                viewModel.logSleep(hours, quality, notes, bedtime, wakeTime, dateString)
            }
        )
    }

    if (showDebriefDialog) {
        val prefs = context.getSharedPreferences("taskpulse_prefs", Context.MODE_PRIVATE)
        val savedReflection = prefs.getString("daily_reflection", "") ?: ""
        DailyDebriefDialog(
            dashboardState = dashboardState,
            initialReflection = savedReflection,
            onDismiss = { showDebriefDialog = false },
            onSaveReflection = { newReflection ->
                prefs.edit().putString("daily_reflection", newReflection).apply()
            }
        )
    }

    if (showIntakeLogDialog) {
        IntakeLogDialog(
            onDismiss = { showIntakeLogDialog = false },
            onSubmit = { type, amount, unit, caffeineMg, timeString, hourOfDay, notes, dateString ->
                viewModel.addIntake(type, amount, unit, caffeineMg, timeString, hourOfDay, notes, dateString)
                QuickLogWidgetProvider.updateAllWidgets(context)
                if (caffeineMg > 0 && hourOfDay >= 14) {
                    val remainingAtBedtime = (caffeineMg * Math.pow(0.5, ((23.5 - hourOfDay) / 5.5))).toInt().coerceAtLeast(1)
                    com.example.taskpulse.notifications.ReminderManager.scheduleWindDownReminder(context, type.displayName, caffeineMg, hourOfDay, remainingAtBedtime)
                    Toast.makeText(context, "🌙 Late intake: Wind-down alert set (~${remainingAtBedtime}mg at bedtime)", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
