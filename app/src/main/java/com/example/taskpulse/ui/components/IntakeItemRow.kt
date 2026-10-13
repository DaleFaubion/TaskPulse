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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.model.IntakeLog
import com.example.taskpulse.data.model.IntakeType
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassBorder
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary

@Composable
fun IntakeItemRow(
    intake: IntakeLog,
    onDelete: () -> Unit
) {
    val accentColor = when (intake.type) {
        IntakeType.WATER -> ElectricCyan
        IntakeType.COFFEE -> AmberGlow
        IntakeType.ENERGY_DRINK -> FlameRose
        IntakeType.TEA -> EmeraldGreen
        IntakeType.SODA -> NeonViolet
        IntakeType.OTHER -> ElectricCyan
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Emoji Icon Container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape)
                ) {
                    Text(text = intake.type.icon, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = intake.type.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Date Pill
                        val dayLabel = com.example.taskpulse.util.DateUtils.formatDisplayDate(intake.dateString)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GlassSurfaceVariant)
                                .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "📅 $dayLabel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Time Pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GlassSurfaceVariant)
                                .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏰ ${intake.timeString}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (intake.caffeineMg > 0 && intake.hourOfDay >= 14) FlameRose else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${if (intake.amount % 1f == 0f) intake.amount.toInt().toString() else "%.1f".format(intake.amount)} ${intake.unit}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        if (intake.caffeineMg > 0) {
                            Text(
                                text = "⚡ ${intake.caffeineMg} mg caffeine",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (intake.hourOfDay >= 14) FlameRose else AmberGlow
                            )
                        } else {
                            Text(
                                text = "💧 0 caffeine",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }

                    if (intake.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = intake.notes,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            // Delete Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GlassSurfaceVariant)
                    .clickable { onDelete() }
            ) {
                Text(text = "✕", fontSize = 13.sp, color = TextMuted)
            }
        }
    }
}
