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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassBorder
import com.example.taskpulse.theme.GlassSurface
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.MidnightBackground
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.util.DateUtils

@Composable
fun AddDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, description: String, priority: Priority, category: String, dueDate: String) -> Unit,
    onAddHabitDates: (title: String, category: String, completedDates: Set<String>) -> Unit = { _, _, _ -> },
    onAddHabit: (title: String, category: String, completedDays: Set<Int>) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Task, 1 = Habit
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDueDate by remember { mutableStateOf("Today") }
    var habitCompletedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var habitCompletedDays by remember { mutableStateOf<Set<Int>>(emptySet()) }

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
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create New Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GlassSurfaceVariant,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonViolet
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Task", color = if (selectedTab == 0) NeonViolet else TextMuted) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Habit", color = if (selectedTab == 1) ElectricCyan else TextMuted) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonViolet,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = NeonViolet,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Due Date", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Today", "Tomorrow", "This Week").forEach { dueOption ->
                            val isSelected = selectedDueDate == dueOption
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else GlassSurfaceVariant)
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, NeonViolet, RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable { selectedDueDate = dueOption }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = dueOption,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Priority", fontSize = 13.sp, color = TextMuted)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Priority.entries.forEach { priority ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedPriority == priority,
                                    onClick = { selectedPriority = priority },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonViolet)
                                )
                                Text(
                                    text = priority.name,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    Text("Initial Completed Days This Week", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    val weekDays = remember { DateUtils.getCurrentWeekDates() }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        weekDays.forEach { dayInfo ->
                            val isSelected = dayInfo.dateString in habitCompletedDates
                            val isToday = dayInfo.isToday
                            val isFuture = dayInfo.isFuture
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 38.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> FlameRose.copy(alpha = 0.95f)
                                            isToday -> ElectricCyan.copy(alpha = 0.2f)
                                            isFuture -> GlassSurfaceVariant.copy(alpha = 0.4f)
                                            else -> GlassSurfaceVariant
                                        }
                                    )
                                    .then(
                                        if (isToday) Modifier.border(1.5.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                        else if (isSelected) Modifier.border(1.dp, FlameRose, RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        habitCompletedDates = if (isSelected) {
                                            habitCompletedDates - dayInfo.dateString
                                        } else {
                                            habitCompletedDates + dayInfo.dateString
                                        }
                                    }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayInfo.dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TextPrimary else if (isToday) ElectricCyan else if (isFuture) TextMuted.copy(alpha = 0.5f) else TextMuted
                                    )
                                    Text(
                                        text = dayInfo.dayNumber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) TextPrimary else if (isToday) ElectricCyan else if (isFuture) TextMuted.copy(alpha = 0.5f) else TextMuted.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Health, Work)", color = TextMuted) },
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
                            if (selectedTab == 0) {
                                onAddTask(title, description, selectedPriority, category, selectedDueDate)
                            } else {
                                onAddHabitDates(title, category, habitCompletedDates)
                                onAddHabit(title, category, habitCompletedDays)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) NeonViolet else ElectricCyan,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
