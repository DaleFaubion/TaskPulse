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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.model.Priority
import com.example.taskpulse.data.model.Task
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary

@Composable
fun TaskItemRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (priorityColor, priorityText) = when (task.priority) {
        Priority.HIGH -> Pair(FlameRose, "HIGH")
        Priority.MEDIUM -> Pair(AmberGlow, "MED")
        Priority.LOW -> Pair(EmeraldGreen, "LOW")
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        cornerRadius = 14.dp,
        contentPadding = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonViolet,
                    uncheckedColor = TextMuted,
                    checkmarkColor = TextPrimary
                ),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Priority Glow Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = priorityText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📁 ${task.category}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    val (dueBadgeBg, dueBadgeColor, dueText) = when {
                        task.dueDate.contains("Overdue", ignoreCase = true) -> Triple(FlameRose.copy(alpha = 0.2f), FlameRose, "⚠️ ${task.dueDate}")
                        task.dueDate.contains("Today", ignoreCase = true) -> Triple(AmberGlow.copy(alpha = 0.2f), AmberGlow, "⚡ ${task.dueDate}")
                        task.dueDate.contains("Tomorrow", ignoreCase = true) -> Triple(ElectricCyan.copy(alpha = 0.15f), ElectricCyan, "📅 ${task.dueDate}")
                        else -> Triple(GlassSurfaceVariant, TextMuted, "📅 ${task.dueDate}")
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(dueBadgeBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = dueText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = dueBadgeColor
                        )
                    }
                }
            }

            // Delete action button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(26.dp)
            ) {
                Text(
                    text = "✕",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
