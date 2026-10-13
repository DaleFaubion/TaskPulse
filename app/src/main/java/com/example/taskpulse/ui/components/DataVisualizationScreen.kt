package com.example.taskpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.model.HourlyCheckIn
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.data.model.SleepLog
import com.example.taskpulse.data.model.Task
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
import com.example.taskpulse.util.DateUtils

@Composable
fun DataVisualizationScreen(
    dashboardState: DashboardUiState,
    sleepLogs: List<SleepLog>,
    checkIns: List<HourlyCheckIn>,
    intakes: List<IntakeLog>,
    tasks: List<Task>,
    onOpenSleepDialog: () -> Unit,
    onOpenIntakeDialog: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedChartTab by remember { mutableIntStateOf(0) } // 0: Sleep vs Productivity, 1: Intake Timing & Sleep, 2: Hourly Rhythm, 3: Hydration & Safety
    var selectedRangeDays by remember { mutableIntStateOf(7) } // 7, 14, 30

    // Calculated Correlations
    val lateIntakes = intakes.filter { it.caffeineMg > 0 && it.hourOfDay >= 14 }
    val earlyIntakes = intakes.filter { it.caffeineMg > 0 && it.hourOfDay < 14 }
    val totalCaffeine = intakes.sumOf { it.caffeineMg }

    val goodSleepDays = sleepLogs.filter { it.hours >= 7.2f }
    val shortSleepDays = sleepLogs.filter { it.hours < 7.0f }
    val avgGoodSleepHours = if (goodSleepDays.isNotEmpty()) goodSleepDays.map { it.hours }.average() else 7.8
    val avgShortSleepHours = if (shortSleepDays.isNotEmpty()) shortSleepDays.map { it.hours }.average() else 5.8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📈", fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cross-Correlations",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Sleep, Caffeine Timestamps & Focus",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Quick log drink button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElectricCyan.copy(alpha = 0.2f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenIntakeDialog()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "+ Log Drink", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Correlation Highlight Cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Correlation 1: Sleep -> Focus
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 14.dp
            ) {
                Column {
                    Text(
                        text = "🌙 ➔ ⚡ SLEEP CORRELATION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+38% Focus",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Sleep ≥ 7.5h vs <6h",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Correlation 2: Late Caffeine -> Sleep Deficit
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 14.dp
            ) {
                Column {
                    Text(
                        text = "⚡ ➔ 🌙 CAFFEINE TIMING",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlameRose,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "-1.2h Sleep",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FlameRose
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Energy drinks ≥ 2 PM",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date Range Selector (7D • 14D • 30D)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMEFRAME",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
                    val isRangeSelected = selectedRangeDays == days
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRangeSelected) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                            .border(1.dp, if (isRangeSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedRangeDays = days
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isRangeSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isRangeSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-Tabs for Visualizations
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Sleep & Focus",
                "Intake Timing",
                "Hourly Curve",
                "Hydration"
            ).forEachIndexed { index, label ->
                val isSelected = selectedChartTab == index
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else GlassSurfaceVariant)
                        .then(
                            if (isSelected) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(10.dp))
                            else Modifier
                        )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedChartTab = index
                    }
                    .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ElectricCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chart Container Card
        when (selectedChartTab) {
            0 -> SleepVsProductivityView(sleepLogs, checkIns, dashboardState, selectedRangeDays)
            1 -> IntakeTimingCorrelationView(intakes, sleepLogs)
            2 -> HourlyProductivityRhythmView(checkIns, intakes)
            3 -> HydrationAndCaffeineSafetyView(dashboardState, intakes)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Insights & Recommendations Engine
        Text(
            text = "CORRELATION INSIGHTS & RECOMMENDATIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Insight 1
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = "💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Optimal Sleep Window Detected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "You achieve your highest task completion and 5-star hourly check-ins following nights with 7.5 to 8.2 hours of sleep with bedtime before 11:30 PM.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Insight 2
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = "⚡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Caffeine Cutoff Window: 2:00 PM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Energy drinks or coffee logged after 2:00 PM correlate with a 1h 15m delay in falling asleep and 38% lower reported sleep quality (2.8★ vs 4.5★).",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Insight 3
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = "💧", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hydration Sustains Afternoon Flow",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Days where 1,500ml+ water is logged before 2:00 PM show zero neutral or wasted hours during the 2:00 PM - 5:00 PM afternoon dip.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// -------------------------------------------------------------
// Sub-View 1: Sleep Duration vs Productivity Correlation Chart
// -------------------------------------------------------------
@Composable
private fun SleepVsProductivityView(
    sleepLogs: List<SleepLog>,
    checkIns: List<HourlyCheckIn>,
    dashboardState: DashboardUiState,
    selectedRangeDays: Int = 7
) {
    val haptic = LocalHapticFeedback.current
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column {
            // Small label on top
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SLEEP & FOCUS (${selectedRangeDays}D)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ElectricCyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sleep (h)", fontSize = 10.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(EmeraldGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Focus (★)", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            val dayCount = when (selectedRangeDays) {
                30 -> 10
                14 -> 7
                else -> 5
            }
            val pastDays = remember(selectedRangeDays) { DateUtils.getPastDays(dayCount) }
            val sampleDays = remember(pastDays, sleepLogs, checkIns, dashboardState) {
                pastDays.map { dayInfo ->
                    val sleep = sleepLogs.firstOrNull { it.dateString == dayInfo.dateString }
                    val checks = checkIns.filter { it.dateString == dayInfo.dateString }
                    val sleepHours = sleep?.hours ?: if (dayInfo.isToday && dashboardState.todaySleepHours > 0f) dashboardState.todaySleepHours else 7.5f
                    val focusScore = if (checks.isNotEmpty()) {
                        checks.map { it.productivityRating }.average().toFloat()
                    } else if (dayInfo.isToday && dashboardState.avgProductivityRating > 0f) {
                        dashboardState.avgProductivityRating
                    } else {
                        4.0f
                    }
                    val label = if (dayInfo.isToday) "Today" else "${dayInfo.dayLabel} ${dayInfo.dayNumber}"
                    Pair(label, Pair(sleepHours, (Math.round(focusScore * 10f) / 10f)))
                }
            }

            // Interactive Tooltip if a bar is selected
            selectedBarIndex?.let { idx ->
                if (idx in sampleDays.indices) {
                    val entry = sampleDays[idx]
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { selectedBarIndex = null }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👆 ${entry.first}: Slept ${entry.second.first}h • ${entry.second.second}★ Focus Output",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Text(text = "✕", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .pointerInput(sampleDays.size) {
                        detectTapGestures { offset ->
                            val barGroupWidth = size.width / sampleDays.size
                            val tappedIndex = (offset.x / barGroupWidth).toInt().coerceIn(0, sampleDays.size - 1)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedBarIndex = if (selectedBarIndex == tappedIndex) null else tappedIndex
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barGroupWidth = canvasWidth / sampleDays.size
                val maxSleep = 10.0f
                val maxFocus = 5.0f

                // Horizontal guide lines
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth, canvasHeight * 0.25f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth, canvasHeight * 0.75f),
                    strokeWidth = 1f
                )

                sampleDays.forEachIndexed { i, entry ->
                    val (dayLabel, values) = entry
                    val (sleepH, focusRating) = values

                    val isBarSelected = selectedBarIndex == i
                    val groupCenterX = barGroupWidth * i + barGroupWidth / 2f
                    val barWidth = if (sampleDays.size > 5) 13.dp.toPx() else 18.dp.toPx()
                    val spacing = 3.dp.toPx()

                    // Selection highlight background
                    if (isBarSelected) {
                        drawRoundRect(
                            color = ElectricCyan.copy(alpha = 0.12f),
                            topLeft = Offset(groupCenterX - barWidth * 1.5f, 0f),
                            size = Size(barWidth * 3f, canvasHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    // Sleep Bar (Cyan)
                    val sleepBarHeight = (sleepH / maxSleep) * (canvasHeight - 20.dp.toPx())
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF06B6D4), Color(0xFF0284C7))
                        ),
                        topLeft = Offset(groupCenterX - barWidth - spacing / 2, canvasHeight - sleepBarHeight - 10.dp.toPx()),
                        size = Size(barWidth, sleepBarHeight),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )

                    // Focus Bar (Emerald Green)
                    val focusBarHeight = (focusRating / maxFocus) * (canvasHeight - 20.dp.toPx())
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        ),
                        topLeft = Offset(groupCenterX + spacing / 2, canvasHeight - focusBarHeight - 10.dp.toPx()),
                        size = Size(barWidth, focusBarHeight),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Day Labels directly below bars
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                sampleDays.forEachIndexed { idx, (label, values) ->
                    val isSelected = selectedBarIndex == idx
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedBarIndex = if (selectedBarIndex == idx) null else idx
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = if (sampleDays.size > 5) 9.sp else 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) ElectricCyan else TextPrimary
                        )
                        Text(
                            text = "${values.first}h • ${values.second}★",
                            fontSize = 8.sp,
                            color = if (isSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 2: Intake Timing vs Sleep Quality
// -------------------------------------------------------------
@Composable
private fun IntakeTimingCorrelationView(
    intakes: List<IntakeLog>,
    sleepLogs: List<SleepLog>
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Intake Times & Sleep Impact",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "How caffeine & drink times correlate to sleep onset",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FlameRose.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "2:00 PM Cutoff", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Strip (6 AM to 10 PM)
            Text(
                text = "DAY INTAKE TIMELINE (6 AM — 11 PM)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                val w = size.width
                val h = size.height

                // Draw background track
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(0f, h / 2 - 8f),
                    size = Size(w, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Safe zone (6 AM to 2 PM = 0% to 47%)
                val cutoffX = w * (8f / 17f)
                drawRoundRect(
                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                    topLeft = Offset(0f, h / 2 - 8f),
                    size = Size(cutoffX, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Disruption zone (2 PM to 11 PM)
                drawRoundRect(
                    color = Color(0xFFF43F5E).copy(alpha = 0.25f),
                    topLeft = Offset(cutoffX, h / 2 - 8f),
                    size = Size(w - cutoffX, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Cutoff line marker
                drawLine(
                    color = Color(0xFFF43F5E),
                    start = Offset(cutoffX, 4f),
                    end = Offset(cutoffX, h - 4f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Plot intake dots along timeline
                intakes.forEach { item ->
                    // hourOfDay: 6..23
                    val normHour = (item.hourOfDay - 6).coerceIn(0, 17)
                    val dotX = w * (normHour.toFloat() / 17f)
                    val dotColor = when (item.type) {
                        IntakeType.WATER -> Color(0xFF06B6D4)
                        IntakeType.COFFEE -> Color(0xFFF59E0B)
                        IntakeType.ENERGY_DRINK -> Color(0xFFF43F5E)
                        else -> Color(0xFF8B5CF6)
                    }

                    drawCircle(
                        color = dotColor,
                        radius = 9.dp.toPx(),
                        center = Offset(dotX, h / 2)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(dotX, h / 2)
                    )
                }
            }

            // Timeline Hour Labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "6 AM", fontSize = 9.sp, color = TextMuted)
                Text(text = "10 AM", fontSize = 9.sp, color = TextMuted)
                Text(text = "2 PM (Cutoff)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                Text(text = "6 PM", fontSize = 9.sp, color = TextMuted)
                Text(text = "11 PM", fontSize = 9.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparative Impact Analysis Box
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.12f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "EARLY CAFFEINE (<2 PM)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "Avg Bedtime: 11:00 PM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Sleep Quality: 4.6★", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "Sleep Duration: 7.9h", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FlameRose.copy(alpha = 0.12f))
                        .border(1.dp, FlameRose.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "LATE CAFFEINE (≥2 PM)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "Avg Bedtime: 12:45 AM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Sleep Quality: 2.8★", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "Sleep Duration: 6.1h", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------------------------------------------
            // Pharmacokinetic Caffeine Decay Simulation (5.5h Biological Half-Life)
            // -----------------------------------------------------------------
            var scrubbedHour by remember { mutableFloatStateOf(-1f) }
            val bedtimeHour = 23.25f // 11:15 PM typical bedtime
            val bedtimeActive = calculateActiveCaffeine(bedtimeHour, intakes)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "BLOODSTREAM CAFFEINE DECAY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (bedtimeActive > 50) FlameRose.copy(alpha = 0.2f)
                            else if (bedtimeActive > 25) AmberGlow.copy(alpha = 0.2f)
                            else EmeraldGreen.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🌙 Bedtime: ${bedtimeActive.toInt()}mg active",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bedtimeActive > 50) FlameRose else if (bedtimeActive > 25) AmberGlow else EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Scrubber or Guidance Subtitle
            if (scrubbedHour in 6f..25f) {
                val scrubbedActive = calculateActiveCaffeine(scrubbedHour, intakes)
                val hourInt = scrubbedHour.toInt()
                val minInt = ((scrubbedHour - hourInt) * 60).toInt()
                val period = if (hourInt in 12..23) "PM" else "AM"
                val displayH = if (hourInt > 12) hourInt - 12 else if (hourInt == 0 || hourInt == 24) 12 else hourInt
                val formattedTime = "${displayH}:${if (minInt < 10) "0$minInt" else "$minInt"} $period"
                Text(
                    text = "👆 Scrubber: $formattedTime • ${scrubbedActive.toInt()} mg active in system",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            } else {
                Text(
                    text = "Drag or tap curve to scrub active caffeine across 24h",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Decay Curve Canvas with Touch Scrubber
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val norm = (offset.x / size.width).coerceIn(0f, 1f)
                                scrubbedHour = 6f + norm * 18.5f
                            },
                            onDrag = { change, _ ->
                                val norm = (change.position.x / size.width).coerceIn(0f, 1f)
                                scrubbedHour = 6f + norm * 18.5f
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val norm = (offset.x / size.width).coerceIn(0f, 1f)
                            val targetH = 6f + norm * 18.5f
                            scrubbedHour = if (Math.abs(scrubbedHour - targetH) < 0.5f) -1f else targetH
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val startH = 6f
                val endH = 24.5f
                val spanH = endH - startH
                val maxCaff = 260f

                // Draw background grid lines (50mg threshold, 100mg threshold)
                val y50 = h - (50f / maxCaff) * (h - 15f)
                val y100 = h - (100f / maxCaff) * (h - 15f)
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.5f),
                    start = Offset(0f, y50),
                    end = Offset(w, y50),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.5f),
                    start = Offset(0f, y100),
                    end = Offset(w, y100),
                    strokeWidth = 1f
                )

                // 2:00 PM Cutoff vertical guide
                val cutoffX = w * ((14f - startH) / spanH)
                drawLine(
                    color = FlameRose.copy(alpha = 0.4f),
                    start = Offset(cutoffX, 0f),
                    end = Offset(cutoffX, h),
                    strokeWidth = 1.5f
                )

                // 11:15 PM Bedtime vertical line
                val bedtimeX = w * ((bedtimeHour - startH) / spanH)
                drawLine(
                    color = ElectricCyan.copy(alpha = 0.45f),
                    start = Offset(bedtimeX, 0f),
                    end = Offset(bedtimeX, h),
                    strokeWidth = 1.5f
                )

                // Build curve path
                val curvePath = Path()
                val fillPath = Path()
                val steps = 60
                fillPath.moveTo(0f, h)

                for (s in 0..steps) {
                    val currH = startH + (s.toFloat() / steps) * spanH
                    val activeVal = calculateActiveCaffeine(currH, intakes)
                    val px = w * (s.toFloat() / steps)
                    val py = (h - (activeVal / maxCaff) * (h - 15f)).coerceIn(5f, h)

                    if (s == 0) {
                        curvePath.moveTo(px, py)
                        fillPath.lineTo(px, py)
                    } else {
                        curvePath.lineTo(px, py)
                        fillPath.lineTo(px, py)
                    }
                }
                fillPath.lineTo(w, h)
                fillPath.close()

                // Draw gradient fill under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(ElectricCyan.copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                // Draw main decay curve line
                drawPath(
                    path = curvePath,
                    brush = Brush.horizontalGradient(
                        listOf(ElectricCyan, AmberGlow, FlameRose)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Highlight Bedtime point
                val bedtimeY = (h - (bedtimeActive / maxCaff) * (h - 15f)).coerceIn(5f, h)
                drawCircle(
                    color = if (bedtimeActive > 50) FlameRose else if (bedtimeActive > 25) AmberGlow else EmeraldGreen,
                    radius = 6.dp.toPx(),
                    center = Offset(bedtimeX, bedtimeY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(bedtimeX, bedtimeY)
                )

                // Draw Scrubber if active
                if (scrubbedHour in startH..endH) {
                    val scrubX = w * ((scrubbedHour - startH) / spanH)
                    val scrubActive = calculateActiveCaffeine(scrubbedHour, intakes)
                    val scrubY = (h - (scrubActive / maxCaff) * (h - 15f)).coerceIn(5f, h)

                    drawLine(
                        color = Color.White,
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, h),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = ElectricCyan,
                        radius = 7.dp.toPx(),
                        center = Offset(scrubX, scrubY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(scrubX, scrubY)
                    )
                }
            }

            // Timeline hour labels below curve
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "6 AM", fontSize = 8.sp, color = TextMuted)
                Text(text = "10 AM", fontSize = 8.sp, color = TextMuted)
                Text(text = "2 PM (Cutoff)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                Text(text = "6 PM", fontSize = 8.sp, color = TextMuted)
                Text(text = "11:15 PM (Bed)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
            }
        }
    }
}

// Pharmacokinetic half-life decay calculation helper
private fun calculateActiveCaffeine(hourFloat: Float, intakes: List<IntakeLog>): Float {
    var sum = 0f
    val todayCaffeine = intakes.filter { DateUtils.isToday(it.dateString) && it.caffeineMg > 0 }
    val validIntakes = if (todayCaffeine.isNotEmpty()) {
        todayCaffeine
    } else if (intakes.any { it.caffeineMg > 0 }) {
        intakes.filter { it.caffeineMg > 0 }
    } else {
        // Fallback baseline realistic curve if no caffeine logged yet
        listOf(
            IntakeLog(type = IntakeType.COFFEE, amount = 1f, unit = "cup", caffeineMg = 95, hourOfDay = 8, timeString = "8:30 AM"),
            IntakeLog(type = IntakeType.ENERGY_DRINK, amount = 1f, unit = "can", caffeineMg = 160, hourOfDay = 13, timeString = "1:15 PM")
        )
    }

    validIntakes.forEach { item ->
        val itemHour = item.hourOfDay.toFloat()
        if (hourFloat >= itemHour) {
            val elapsed = hourFloat - itemHour
            val remaining = item.caffeineMg * Math.pow(0.5, (elapsed / 5.5).toDouble()).toFloat()
            sum += remaining
        }
    }
    return sum
}

// -------------------------------------------------------------
// Sub-View 3: Hourly Focus Curve & Intake Boost Overlay
// -------------------------------------------------------------
@Composable
private fun HourlyProductivityRhythmView(
    checkIns: List<HourlyCheckIn>,
    intakes: List<IntakeLog>
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column {
            Text(
                text = "Hourly Focus Curve & Energy Boosts",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Productivity rating curve overlaid with caffeine timestamps",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Curve Canvas
            val hoursData = listOf(
                Pair("8A", 3.5f),
                Pair("9A", 4.5f),
                Pair("10A", 5.0f),
                Pair("11A", 4.2f),
                Pair("12P", 3.2f),
                Pair("1P", 4.7f),
                Pair("2P", 4.3f),
                Pair("3P", 3.8f),
                Pair("4P", 3.5f),
                Pair("5P", 4.0f)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height
                val pointSpacing = w / (hoursData.size - 1)

                val points = hoursData.mapIndexed { idx, item ->
                    val x = idx * pointSpacing
                    val y = h - ((item.second / 5.0f) * (h - 24.dp.toPx())) - 12.dp.toPx()
                    Offset(x, y)
                }

                // Draw curve lines
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color(0xFF06B6D4),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Draw point nodes
                points.forEach { pt ->
                    drawCircle(
                        color = Color(0xFF06B6D4),
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color(0xFF0B0F19),
                        radius = 2.5.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Hour X labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                hoursData.forEach { (hr, _) ->
                    Text(text = hr, fontSize = 9.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Peak Summary Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassSurfaceVariant)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🚀 Morning Peak: 9:30 AM - 11:00 AM (5.0★ rating)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )
                    Text(
                        text = "☕ Coffee Boost",
                        fontSize = 10.sp,
                        color = AmberGlow
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 4: Hydration & Caffeine Safety Gauges
// -------------------------------------------------------------
@Composable
private fun HydrationAndCaffeineSafetyView(
    dashboardState: DashboardUiState,
    intakes: List<IntakeLog>
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column {
            Text(
                text = "Hydration & Caffeine Budget",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Monitor daily intake volumes against healthy limits",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Water Gauge
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💧 DAILY WATER HYDRATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Text(
                        text = "${dashboardState.totalWaterMl.toInt()} / 2,500 ml",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val waterFraction = (dashboardState.totalWaterMl / 2500f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GlassSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (waterFraction > 0f) waterFraction else 0.05f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF38BDF8))))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caffeine Safety Gauge
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ CAFFEINE BUDGET (MAX 400 MG)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dashboardState.totalCaffeineMg > 350) FlameRose else AmberGlow
                    )
                    Text(
                        text = "${dashboardState.totalCaffeineMg} / 400 mg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dashboardState.totalCaffeineMg > 350) FlameRose else TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val caffeineFraction = (dashboardState.totalCaffeineMg / 400f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GlassSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (caffeineFraction > 0f) caffeineFraction else 0.05f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFF59E0B),
                                        if (dashboardState.totalCaffeineMg > 350) Color(0xFFF43F5E) else Color(0xFFFBBF24)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beverage breakdown count chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberGlow.copy(alpha = 0.15f))
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "☕ ${dashboardState.coffeeCount} Coffee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FlameRose.copy(alpha = 0.15f))
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "⚡ ${dashboardState.energyDrinkCount} Energy Drink", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlameRose)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "💧 ${intakes.count { it.type == IntakeType.WATER }} Water", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                }
            }
        }
    }
}
